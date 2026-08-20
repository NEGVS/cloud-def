package xCloud.dto.recruitment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * JD生成请求DTO
 * @author Claude
 * @date 2026-08-18
 */
@Data
@Schema(description = "JD生成请求")
public class JDGenerationRequest {

    @NotBlank(message = "岗位标题不能为空")
    @Schema(description = "岗位标题", example = "高级Java后端工程师")
    private String title;

    @NotBlank(message = "需求描述不能为空")
    @Schema(description = "岗位需求描述（HR自然语言描述）",
            example = "需要招聘一名高级Java工程师，3-5年经验，熟悉Spring Boot、微服务、MySQL、Redis，有高并发经验优先")
    private String requirement;

    @Schema(description = "薪资范围", example = "20k-35k")
    private String salaryRange;

    @Schema(description = "工作地点", example = "北京-朝阳区")
    private String location;

    @Schema(description = "学历要求", example = "本科")
    private String education;

    @Schema(description = "工作经验要求（年）", example = "3")
    private Integer experienceYears;

    @Schema(description = "公司介绍")
    private String companyIntro;

    @Schema(description = "生成策略（standard-标准，attractive-吸引人，concise-简洁）", example = "attractive")
    private String generationStrategy = "standard";

    @Schema(description = "HR用户ID")
    private Long hrUserId;
}
