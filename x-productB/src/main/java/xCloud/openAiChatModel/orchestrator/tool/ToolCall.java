package xCloud.openAiChatModel.orchestrator.tool;

import lombok.Data;

import java.util.Map;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/5 15:11
 * @ClassName ToolCall
 */
@Data
public class ToolCall {

    private String tool;

    private Map<String, Object> args;
}
