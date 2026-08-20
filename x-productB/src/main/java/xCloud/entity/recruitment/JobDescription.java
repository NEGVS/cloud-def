package xCloud.entity.recruitment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * JD（岗位描述）实体
 * 用途：存储AI生成的岗位描述及优化历史
 * @author Claude
 * @date 2026-08-18
 */
@Data
@Accessors(chain = true)
@TableName("job_description")
public class JobDescription {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * JD标题
     */
    private String title;

    /**
     * 岗位职责（AI生成）
     */
    private String responsibilities;

    /**
     * 任职要求（AI生成）
     */
    private String requirements;

    /**
     * 薪资范围
     */
    private String salaryRange;

    /**
     * 工作地点
     */
    private String location;

    /**
     * 学历要求（本科、硕士、博士）
     */
    private String education;

    /**
     * 工作经验要求（年）
     */
    private Integer experienceYears;

    /**
     * 技能标签（逗号分隔）
     */
    private String skillTags;

    /**
     * 岗位亮点/福利
     */
    private String highlights;

    /**
     * 公司介绍
     */
    private String companyIntro;

    /**
     * JD状态（draft-草稿，review-待审核，published-已发布，archived-已归档）
     */
    private String status;

    /**
     * 生成版本号（每次优化递增）
     */
    private Integer version;

    /**
     * 原始需求输入（HR的需求描述）
     */
    private String originalRequirement;

    /**
     * HR反馈内容（JSON格式，存储多轮反馈）
     */
    private String feedbackHistory;

    /**
     * 生成策略（standard-标准，attractive-吸引人，concise-简洁）
     */
    private String generationStrategy;

    /**
     * 创建人ID（HR用户ID）
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;

    /**
     * 发布时间
     */
    private Date publishedAt;

    /**
     * 是否删除（0-否，1-是）
     */
    private Integer deleted;
}
