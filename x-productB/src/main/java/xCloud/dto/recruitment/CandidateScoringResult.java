package xCloud.dto.recruitment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 候选人评分结果VO
 * @author Claude
 * @date 2026-08-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateScoringResult {

    /**
     * 候选人ID
     */
    private Long candidateId;

    /**
     * 候选人姓名
     */
    private String candidateName;

    /**
     * JD ID
     */
    private Long jdId;

    /**
     * 总分（0-100）
     */
    private BigDecimal totalScore;

    /**
     * 各维度分数
     */
    private DimensionScores dimensionScores;

    /**
     * 匹配理由
     */
    private String matchReason;

    /**
     * 风险分析
     */
    private String riskAnalysis;

    /**
     * 优势列表
     */
    private List<String> advantages;

    /**
     * 劣势列表
     */
    private List<String> disadvantages;

    /**
     * 推荐度
     */
    private String recommendation;

    /**
     * 推荐理由
     */
    private String recommendationReason;

    /**
     * 各维度分数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionScores {
        private BigDecimal skillScore;          // 技能匹配分
        private BigDecimal experienceScore;     // 经验匹配分
        private BigDecimal educationScore;      // 学历匹配分
        private BigDecimal salaryScore;         // 薪资匹配分
        private BigDecimal locationScore;       // 地点匹配分
        private BigDecimal projectScore;        // 项目经验分
        private BigDecimal stabilityScore;      // 稳定性分
    }
}
