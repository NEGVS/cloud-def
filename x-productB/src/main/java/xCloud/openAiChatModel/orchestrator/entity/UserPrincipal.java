package xCloud.openAiChatModel.orchestrator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/5 14:56
 * @ClassName UserPrincipal
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String username;
    private List<String> roles;
    private List<String> skills;
    private Long resumeId;
}
