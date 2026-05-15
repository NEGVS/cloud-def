package xCloud.service.recruitment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
 *   长期摘要：超出 MAX_TURNS 轮后，用 LLM 摘要压缩历史，存为 system message
 *   本地缓存：ConcurrentHashMap 作为一级缓存，减少 Redis 读写次数
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

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 本地一级缓存：避免每次都读 Redis
     * key=sessionId, value=消息列表
     */
    private final Map<String, LinkedList<Map<String, String>>> localCache = new ConcurrentHashMap<>();

    /** Redis 是否可用（启动时检测，运行中动态切换） */
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
        log.info("添加一条消息，同步写本地缓存和 Redis");
        LinkedList<Map<String, String>> history = getOrLoadHistory(sessionId);
        history.add(Map.of("role", role, "content", content));

        // 滑动窗口：超出 MAX_TURNS 轮时移除最早的一轮（2条）
        while (history.size() > MAX_TURNS * 2) {
            history.removeFirst();
        }

        // 同步写 Redis（失败不影响主流程）
        persistToRedis(sessionId, history);
    }

    /**
     * 获取完整对话历史
     */
    public List<Map<String, String>> getHistory(String sessionId) {
        return new ArrayList<>(getOrLoadHistory(sessionId));
    }

    /**
     * 构建历史对话文本（用于 ReAct Prompt 中的 Memory 部分）
     */
    public String buildHistoryText(String sessionId) {
        log.info("构建历史对话文本（用于 ReAct Prompt 中的 Memory 部分）");
        List<Map<String, String>> history = getHistory(sessionId);
        if (history.isEmpty()) return "（无历史对话）";

        return history.stream()
                .map(msg -> {
                    String role = "user".equals(msg.get("role")) ? "用户" : "助手";
                    return role + ": " + msg.get("content");
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * 清除会话（本地缓存 + Redis）
     */
    public void clear(String sessionId) {
        localCache.remove(sessionId);
        if (redisAvailable) {
            try {
                redisTemplate.delete(KEY_PREFIX + sessionId);
            } catch (Exception e) {
                log.warn("[Memory] Redis 清除失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 是否存在会话
     */
    public boolean exists(String sessionId) {
        if (localCache.containsKey(sessionId)) return true;
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
     * 优先从本地缓存读取，缓存未命中则从 Redis 加载，Redis 不可用则返回空列表。
     */
    private LinkedList<Map<String, String>> getOrLoadHistory(String sessionId) {
        // 一级缓存命中，直接返回
        LinkedList<Map<String, String>> cached = localCache.get(sessionId);
        if (cached != null) return cached;

        // 从 Redis 加载
        LinkedList<Map<String, String>> history = loadFromRedis(sessionId);
        localCache.put(sessionId, history);
        return history;
    }

    /**
     * 从 Redis 加载会话历史，失败时返回空列表并标记 Redis 不可用。
     */
    private LinkedList<Map<String, String>> loadFromRedis(String sessionId) {
        log.info("redisAvailable:{}", redisAvailable);
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
}
