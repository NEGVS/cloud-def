package xCloud.service.recruitment;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多轮对话记忆管理
 *
 * 每个 sessionId 维护独立的对话历史，滑动窗口保留最近 MAX_TURNS 轮。
 * 内存存储，重启后清空（生产环境可替换为 Redis）。
 */
@Service
public class ConversationMemoryService {

    private static final int MAX_TURNS = 10; // 最多保留 10 轮（20 条消息）

    /** sessionId → 消息列表 */
    private final Map<String, LinkedList<Map<String, String>>> sessions = new ConcurrentHashMap<>();

    /**
     * 添加一条消息
     *
     * @param sessionId 会话 ID
     * @param role      "user" / "assistant" / "system"
     * @param content   消息内容
     */
    public void addMessage(String sessionId, String role, String content) {
        LinkedList<Map<String, String>> history = sessions.computeIfAbsent(
                sessionId, k -> new LinkedList<>());

        history.add(Map.of("role", role, "content", content));

        // 滑动窗口：超出 MAX_TURNS 轮时移除最早的一轮（2条）
        while (history.size() > MAX_TURNS * 2) {
            history.removeFirst();
        }
    }

    /**
     * 获取完整对话历史（用于拼接 Prompt）
     */
    public List<Map<String, String>> getHistory(String sessionId) {
        return new ArrayList<>(sessions.getOrDefault(sessionId, new LinkedList<>()));
    }

    /**
     * 构建历史对话文本（用于 ReAct Prompt 中的 Memory 部分）
     */
    public String buildHistoryText(String sessionId) {
        List<Map<String, String>> history = getHistory(sessionId);
        if (history.isEmpty()) return "（无历史对话）";

        StringBuilder sb = new StringBuilder();
        for (Map<String, String> msg : history) {
            String role = "user".equals(msg.get("role")) ? "用户" : "助手";
            sb.append(role).append(": ").append(msg.get("content")).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * 清除会话
     */
    public void clear(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * 是否存在会话
     */
    public boolean exists(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}
