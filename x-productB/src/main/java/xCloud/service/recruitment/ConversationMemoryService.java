package xCloud.service.recruitment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 多轮对话记忆管理（Redis 持久化 + 本地缓存双层）
 *
 * 架构：
 *   短期记忆：Redis List，TTL 24h，保留最近 MAX_TURNS 轮原文
 *   本地缓存：LRU 有界一级缓存，减少 Redis 读写次数，按 SESSION_TTL 惰性过期
 *
 * 并发与容量（关键修复）：
 *   1. 本地缓存原为无界 ConcurrentHashMap，只靠 Redis TTL 过期，进程内永不淘汰 → 内存泄漏。
 *      现改为容量有界 + 访问顺序 LRU + 按 SESSION_TTL 惰性过期。
 *   2. 原 getOrLoadHistory 直接返回缓存中的可变 LinkedList，并发同会话请求会互相污染。
 *      现改为：写入在【会话级锁】内完成（读改写原子），对外读取一律返回【拷贝】。
 *   3. Redis 读写放在锁外，避免持锁做网络 IO 拖慢并发。
 *
 * 降级策略：Redis 不可用时自动降级为纯内存模式，不影响主流程。
 */
@Slf4j
@Service
public class ConversationMemoryService {

    /** 最多保留 10 轮（20 条消息）原文 */
    private static final int MAX_TURNS = 10;

    /** Redis key 前缀 */
    private static final String KEY_PREFIX = "chat:session:";

    /** 会话 TTL：24 小时 */
    private static final Duration SESSION_TTL = Duration.ofHours(24);

    /** 本地缓存最大会话数（防止无界增长） */
    private static final int MAX_LOCAL_SESSIONS = 10_000;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 本地一级缓存条目：消息列表 + 过期时间戳（毫秒）
     */
    private static class CacheEntry {
        final LinkedList<Map<String, String>> messages;
        final long expireAt;

        CacheEntry(LinkedList<Map<String, String>> messages, long expireAt) {
            this.messages = messages;
            this.expireAt = expireAt;
        }

        boolean expired(long now) {
            return now > expireAt;
        }
    }

    /**
     * 本地一级缓存：访问顺序 LinkedHashMap（LRU），全部读写由 lruLock 保护
     */
    private final Map<String, CacheEntry> localCache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > MAX_LOCAL_SESSIONS;
        }
    };
    private final Object lruLock = new Object();

    /**
     * 会话级锁：保证同一 session 的"读—改—写"原子，避免并发污染
     */
    private final Map<String, Object> sessionLocks = new ConcurrentHashMap<>();

    /** Redis 是否可用（运行中动态切换） */
    private volatile boolean redisAvailable = true;

    public ConversationMemoryService(StringRedisTemplate redisTemplate,
                                     ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // ─────────────────────────────────────────────
    // 核心操作
    // ─────────────────────────────────────────────

    /**
     * 添加一条消息，同步写本地缓存和 Redis
     *
     * @param sessionId 会话 ID
     * @param role      "user" / "assistant" / "system"
     * @param content   消息内容
     */
    public void addMessage(String sessionId, String role, String content) {
        // 会话级加锁：保证读改写原子，避免并发同会话互相覆盖
        synchronized (lockFor(sessionId)) {
            LinkedList<Map<String, String>> history = loadMergedHistory(sessionId);
            history.add(Map.of("role", role, "content", content));

            // 滑动窗口：超出 MAX_TURNS 轮时移除最早的一轮（2条）
            while (history.size() > MAX_TURNS * 2) {
                history.removeFirst();
            }

            // 回写本地缓存 + Redis
            putLocalCache(sessionId, history);
            persistToRedis(sessionId, history);
        }
    }

    /**
     * 获取完整对话历史（返回拷贝，调用方可安全修改）
     */
    public List<Map<String, String>> getHistory(String sessionId) {
        return new ArrayList<>(loadMergedHistory(sessionId));
    }

    /**
     * 构建历史对话文本（用于 ReAct Prompt 中的 Memory 部分）
     */
    public String buildHistoryText(String sessionId) {
        List<Map<String, String>> history = getHistory(sessionId);
        if (history.isEmpty()) return "（无历史对话）";

        return history.stream()
                .map(msg -> {
                    String role = "user".equals(msg.get("role")) ? "用户" : "助手";
                    return role + ":" + msg.get("content");
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * 清除会话（本地缓存 + Redis）
     */
    public void clear(String sessionId) {
        synchronized (lockFor(sessionId)) {
            synchronized (lruLock) {
                localCache.remove(sessionId);
            }
            if (redisAvailable) {
                try {
                    redisTemplate.delete(KEY_PREFIX + sessionId);
                } catch (Exception e) {
                    log.warn("[Memory] Redis 清除失败: {}", e.getMessage());
                }
            }
        }
        sessionLocks.remove(sessionId);
    }

    /**
     * 是否存在会话
     */
    public boolean exists(String sessionId) {
        synchronized (lruLock) {
            CacheEntry entry = localCache.get(sessionId);
            if (entry != null && !entry.expired(System.currentTimeMillis())) {
                return true;
            }
        }
        if (redisAvailable) {
            try {
                return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + sessionId));
            } catch (Exception e) {
                log.warn("[Memory] Redis exists 查询失败: {}", e.getMessage());
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // 内部：本地缓存 + Redis 双层读取
    // ─────────────────────────────────────────────

    /**
     * 合并加载会话历史：本地缓存命中则直接返回其副本，
     * 未命中则从 Redis 加载并回填本地缓存（回填加 LRU 锁）。
     * <p>
     * 注意：返回的是可修改的【工作副本】，调用方（addMessage）可安全增删，
     * 不影响缓存中的原始对象。
     */
    private LinkedList<Map<String, String>> loadMergedHistory(String sessionId) {
        long now = System.currentTimeMillis();

        // 一级缓存命中（未过期）
        synchronized (lruLock) {
            CacheEntry entry = localCache.get(sessionId);
            if (entry != null && !entry.expired(now)) {
                return new LinkedList<>(entry.messages);
            }
            if (entry != null) {
                localCache.remove(sessionId); // 过期即移除
            }
        }

        // 缓存未命中：从 Redis 加载
        LinkedList<Map<String, String>> history = loadFromRedis(sessionId);

        synchronized (lruLock) {
            localCache.put(sessionId, new CacheEntry(history, now + SESSION_TTL.toMillis()));
        }
        return new LinkedList<>(history);
    }

    /**
     * 写入本地缓存（容量有界 + TTL）
     */
    private void putLocalCache(String sessionId, LinkedList<Map<String, String>> history) {
        long expireAt = System.currentTimeMillis() + SESSION_TTL.toMillis();
        synchronized (lruLock) {
            localCache.put(sessionId, new CacheEntry(new LinkedList<>(history), expireAt));
        }
    }

    /**
     * 从 Redis 加载会话历史，失败时返回空列表并标记 Redis 不可用。
     */
    private LinkedList<Map<String, String>> loadFromRedis(String sessionId) {
        if (!redisAvailable) return new LinkedList<>();
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + sessionId);
            if (json == null || json.isBlank()) return new LinkedList<>();
            List<Map<String, String>> list = objectMapper.readValue(
                    json, new TypeReference<List<Map<String, String>>>() {});
            return new LinkedList<>(list);
        } catch (Exception e) {
            log.warn("[Memory] Redis 加载失败，降级为内存模式: {}", e.getMessage());
            redisAvailable = false;
            return new LinkedList<>();
        }
    }

    /**
     * 将会话历史持久化到 Redis，失败时仅打日志，不抛异常。
     */
    private void persistToRedis(String sessionId, LinkedList<Map<String, String>> history) {
        if (!redisAvailable) return;
        try {
            String json = objectMapper.writeValueAsString(history);
            redisTemplate.opsForValue().set(KEY_PREFIX + sessionId, json, SESSION_TTL);
        } catch (Exception e) {
            log.warn("[Memory] Redis 持久化失败，数据仅在内存中: {}", e.getMessage());
            // 单次失败不立即标记不可用，避免抖动导致频繁切换
        }
    }

    /**
     * 取会话级锁对象（computeIfAbsent 保证同 session 拿到同一个锁）
     */
    private Object lockFor(String sessionId) {
        return sessionLocks.computeIfAbsent(sessionId, k -> new Object());
    }
}
