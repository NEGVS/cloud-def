package xCloud.dto.recruitment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xCloud.entity.recruitment.JobDescription;

/**
 * JD（岗位描述）出参 VO
 * <p>
 * 对外接口统一返回本 VO，避免直接暴露实体；并提供实体转换工厂方法。
 *
 * @author Claude
 * @date 2026-10-12
 */
@Data
@Schema(description = "岗位描述信息")
public class JDVO {

    @Schema(description = "JD ID")
    private Long id;

    @Schema(description = "岗位标题")
    private String title;

    @Schema(description = "岗位职责")
    private String responsibilities;

    @Schema(description = "任职要求")
    private String requirements;

    @Schema(description = "薪资范围")
    private String salaryRange;

    @Schema(description = "工作地点")
    private String location;

    @Schema(description = "学历要求")
    private String education;

    @Schema(description = "工作经验要求（年）")
    private Integer experienceYears;

    @Schema(description = "技能标签（逗号分隔）")
    private String skillTags;

    @Schema(description = "岗位亮点/福利")
    private String highlights;

    @Schema(description = "公司介绍")
    private String companyIntro;

    @Schema(description = "JD状态（draft、review、published、archived）")
    private String status;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "原始需求输入")
    private String originalRequirement;

    @Schema(description = "反馈历史（JSON数组字符串）")
    private String feedbackHistory;

    @Schema(description = "生成策略（standard、attractive、concise）")
    private String generationStrategy;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "发布时间")
    private java.util.Date publishedAt;

    @Schema(description = "创建时间")
    private java.util.Date createdAt;

    @Schema(description = "更新时间")
    private java.util.Date updatedAt;

    @Schema(description = "提示信息，仅生成/优化接口回填")
    private String message;

    @Schema(description = "本次操作耗时（毫秒），仅生成/优化接口回填")
    private Long elapsedMs;

    /**
     * 由实体转换
     */
    public static JDVO from(JobDescription jd) {
        if (jd == null) {
            return null;
        }
        JDVO vo = new JDVO();
        vo.setId(jd.getId());
        vo.setTitle(jd.getTitle());
        vo.setResponsibilities(jd.getResponsibilities());
        vo.setRequirements(jd.getRequirements());
        vo.setSalaryRange(jd.getSalaryRange());
        vo.setLocation(jd.getLocation());
        vo.setEducation(jd.getEducation());
        vo.setExperienceYears(jd.getExperienceYears());
        vo.setSkillTags(jd.getSkillTags());
        vo.setHighlights(jd.getHighlights());
        vo.setCompanyIntro(jd.getCompanyIntro());
        vo.setStatus(jd.getStatus());
        vo.setVersion(jd.getVersion());
        vo.setOriginalRequirement(jd.getOriginalRequirement());
        vo.setFeedbackHistory(jd.getFeedbackHistory());
        vo.setGenerationStrategy(jd.getGenerationStrategy());
        vo.setCreatorId(jd.getCreatorId());
        vo.setPublishedAt(jd.getPublishedAt());
        vo.setCreatedAt(jd.getCreatedAt());
        vo.setUpdatedAt(jd.getUpdatedAt());
        return vo;
    }
}
