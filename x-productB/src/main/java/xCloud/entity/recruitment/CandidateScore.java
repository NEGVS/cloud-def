package xCloud.entity.recruitment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 候选人评分实体
 * @author Claude
 * @date 2026-08-18
 */
@Data
@Accessors(chain = true)
@TableName("candidate_score")
public class CandidateScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 候选人ID
     */
    private Long candidateId;

    /**
     * JD ID
     */
    private Long jdId;

    /**
     * 总分（0-100）
     */
    private BigDecimal totalScore;

    /**
     * 技能匹配分
     */
    private BigDecimal skillScore;

    /**
     * 经验匹配分
     */
    private BigDecimal experienceScore;

    /**
     * 学历匹配分
     */
    private BigDecimal educationScore;

    /**
     * 薪资匹配分
     */
    private BigDecimal salaryScore;

    /**
     * 地点匹配分
     */
    private BigDecimal locationScore;

    /**
     * 项目经验分
     */
    private BigDecimal projectScore;

    /**
     * 稳定性分
     */
    private BigDecimal stabilityScore;

    /**
     * 匹配理由（可解释性）
     */
    private String matchReason;

    /**
     * 风险分析
     */
    private String riskAnalysis;

    /**
     * 优势列表（JSON）
     */
    private String advantages;

    /**
     * 劣势列表（JSON）
     */
    private String disadvantages;

    /**
     * 推荐度（highly_recommend-强烈推荐，recommend-推荐，consider-考虑，not_recommend-不推荐）
     */
    private String recommendation;

    /**
     * 是否应用了HR偏好
     */
    private Integer hrPreferenceApplied;

    /**
     * 评分算法版本
     */
    private String scoringVersion;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
