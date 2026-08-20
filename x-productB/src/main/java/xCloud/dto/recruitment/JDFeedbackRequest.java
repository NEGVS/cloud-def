package xCloud.dto.recruitment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * JD反馈请求DTO
 * @author Claude
 * @date 2026-08-18
 */
@Data
@Schema(description = "JD反馈请求")
public class JDFeedbackRequest {

    @NotNull(message = "JD ID不能为空")
    @Schema(description = "JD ID")
    private Long jdId;

    @NotNull(message = "JD版本号不能为空")
    @Schema(description = "JD版本号")
    private Integer jdVersion;

    @NotBlank(message = "反馈类型不能为空")
    @Schema(description = "反馈类型（adjust-调整优化，approve-通过，reject-拒绝）", example = "adjust")
    private String feedbackType;

    @Schema(description = "反馈内容", example = "岗位职责写得太简单，需要更详细；任职要求中增加对分布式系统的要求")
    private String content;

    @Schema(description = "需要调整的字段（逗号分隔）", example = "responsibilities,requirements")
    private String adjustFields;

    @Schema(description = "HR用户ID")
    private Long hrUserId;
}
