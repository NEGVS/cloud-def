package xCloud.openAiChatModel.orchestrator.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/5 14:44
 * @ClassName RagResponse
 */
@Data
public class RagResponse {

    private String answer;

    private List<Map<String, Object>> chunks;
}
