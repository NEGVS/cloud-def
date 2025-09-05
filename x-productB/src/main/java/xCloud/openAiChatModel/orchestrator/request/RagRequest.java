package xCloud.openAiChatModel.orchestrator.request;

import lombok.Data;

import java.util.Map;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/5 14:43
 * @ClassName RagRequest
 */
@Data
public class RagRequest {
    private String query;
    private Map<String, Object> user_profile;
    private Integer top_k = 6;
    private Map<String, Object> filters;
}
