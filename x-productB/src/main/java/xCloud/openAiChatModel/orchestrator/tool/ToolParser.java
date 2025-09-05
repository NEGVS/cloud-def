package xCloud.openAiChatModel.orchestrator.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/5 15:07
 * @ClassName ToolParser
 * - 用于解析 LLM 回复中的“工具调用 JSON”。
 * - 假设 LLM 可能返回：`{"tool":"apply_job","args":{"job_id":123,"resume_id":456}}`
 */
public class ToolParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Optional<ToolCall> tryParse(String answer) {
        try {
            JsonNode node = mapper.readTree(answer);
            if (node.has("tool")) {
                String tool = node.get("tool").asText();
                JsonNode argsNode = node.get("args");
                Map<String, Object> args = mapper.convertValue(argsNode, Map.class);

//                todo
//                return Optional.of(new ToolCall(tool, args));
                return null;
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
