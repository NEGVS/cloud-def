package xCloud.openAiChatModel.orchestrator.request;

import lombok.Data;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/5 14:50
 * @ClassName ChatReq
 */
@Data
public class ChatReq {

    private String message;
    private String city;
    private String salaryMin;
    private String skills;

}
