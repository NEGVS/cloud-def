package xCloud.openAiChatModel.orchestrator.tool;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/5 15:09
 * @ClassName ToolFormatter
 * - 将工具执行结果与 LLM 原始回答组合，生成最终返回内容。
 * - 支持将 `toolResult` 翻译成用户可读文本。
 */
public class ToolFormatter {
    public static String composeFollowup(String llmAnswer, Object toolResult) {
        StringBuilder sb = new StringBuilder();
        sb.append(llmAnswer).append("\n\n");
        sb.append("【工具执行结果】\n");
        sb.append(toolResult instanceof String ? toolResult : toolResult.toString());
        return sb.toString();
    }
}
