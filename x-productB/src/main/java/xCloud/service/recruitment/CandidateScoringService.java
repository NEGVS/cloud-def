package xCloud.service.recruitment;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xCloud.dto.recruitment.CandidateScoringResult;
import xCloud.entity.es.CandidateResumeDocument;
import xCloud.entity.recruitment.CandidateScore;
import xCloud.entity.recruitment.JobDescription;
import xCloud.mapper.recruitment.CandidateScoreMapper;
import xCloud.mapper.recruitment.JobDescriptionMapper;
import xCloud.repository.CandidateResumeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 候选人评分引擎
 * 核心功能：多维度评分 + 可解释性 + HR偏好驱动
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
@Lazy
public class CandidateScoringService {

    @Autowired
    private CandidateScoreMapper candidateScoreMapper;

    @Autowired
    private JobDescriptionMapper jobDescriptionMapper;

    @Autowired
    private CandidateResumeRepository candidateResumeRepository;

    @Value("${ali.api-key}")
    private String apiKey;

    @Value("${ali.chat_model_name:qwen-plus}")
    private String modelName;

    /**
     * 默认权重配置
     */
    private static final Map<String, BigDecimal> DEFAULT_WEIGHTS = Map.of(
            "skill", new BigDecimal("0.30"),
            "experience", new BigDecimal("0.25"),
            "education", new BigDecimal("0.15"),
            "project", new BigDecimal("0.15"),
            "salary", new BigDecimal("0.10"),
            "stability", new BigDecimal("0.05")
    );

    /**
     * 对候选人进行评分
     * @param candidateId 候选人ID
     * @param jdId JD ID
     * @return 评分结果
     */
    @Transactional(rollbackFor = Exception.class)
    public CandidateScoringResult scoreCandidate(Long candidateId, Long jdId) {
        try {
            log.info("开始候选人评分：candidateId={}, jdId={}", candidateId, jdId);

            // ============1-加载候选人和JD信息============
            Optional<CandidateResumeDocument> candidateOpt = candidateResumeRepository.findById(String.valueOf(candidateId));
            if (!candidateOpt.isPresent()) {
                throw new RuntimeException("候选人不存在：" + candidateId);
            }
            CandidateResumeDocument candidate = candidateOpt.get();

            JobDescription jd = jobDescriptionMapper.selectById(jdId);
            if (jd == null) {
                throw new RuntimeException("JD不存在：" + jdId);
            }

            // ============2-计算各维度分数============
            CandidateScoringResult.DimensionScores dimensionScores = calculateDimensionScores(candidate, jd);

            // ============3-计算总分（加权平均）============
            BigDecimal totalScore = calculateTotalScore(dimensionScores, DEFAULT_WEIGHTS);

            // ============4-使用LLM生成可解释性内容============
            ExplainabilityResult explainability = generateExplainability(candidate, jd, dimensionScores, totalScore);

            // ============5-构建结果============
            CandidateScoringResult result = new CandidateScoringResult();
            result.setCandidateId(candidateId);
            result.setCandidateName(candidate.getName());
            result.setJdId(jdId);
            result.setTotalScore(totalScore);
            result.setDimensionScores(dimensionScores);
            result.setMatchReason(explainability.getMatchReason());
            result.setRiskAnalysis(explainability.getRiskAnalysis());
            result.setAdvantages(explainability.getAdvantages());
            result.setDisadvantages(explainability.getDisadvantages());
            result.setRecommendation(determineRecommendation(totalScore));
            result.setRecommendationReason(explainability.getRecommendationReason());

            // ============6-保存评分记录============
            saveScoringRecord(result, dimensionScores);

            log.info("✅ 候选人评分完成：candidateId={}, totalScore={}, recommendation={}",
                    candidateId, totalScore, result.getRecommendation());
            return result;

        } catch (Exception e) {
            log.error("❌ 候选人评分失败：candidateId={}, jdId={}", candidateId, jdId, e);
            throw new RuntimeException("候选人评分失败：" + e.getMessage(), e);
        }
    }

    /**
     * 计算各维度分数
     */
    private CandidateScoringResult.DimensionScores calculateDimensionScores(CandidateResumeDocument candidate, JobDescription jd) {
        CandidateScoringResult.DimensionScores scores = new CandidateScoringResult.DimensionScores();

        // ============1-技能匹配分============
        scores.setSkillScore(calculateSkillScore(candidate.getSkills(), jd.getSkillTags()));

        // ============2-经验匹配分============
        scores.setExperienceScore(calculateExperienceScore(candidate.getExperience(), jd.getExperienceYears()));

        // ============3-学历匹配分============
        scores.setEducationScore(calculateEducationScore(candidate.getEducation(), jd.getEducation()));

        // ============4-薪资匹配分============
        scores.setSalaryScore(calculateSalaryScore(candidate.getExpectedSalary(), jd.getSalaryRange()));

        // ============5-地点匹配分============
        scores.setLocationScore(calculateLocationScore(candidate.getCity(), jd.getLocation()));

        // ============6-项目经验分（简化版，基于简历内容）============
        scores.setProjectScore(new BigDecimal("75")); // TODO: 基于项目经验详细计算

        // ============7-稳定性分（简化版，基于工作经验）============
        scores.setStabilityScore(new BigDecimal("80")); // TODO: 基于跳槽频率计算

        return scores;
    }

    /**
     * 技能匹配分（Jaccard相似度）
     */
    private BigDecimal calculateSkillScore(String candidateSkills, String jdSkills) {
        if (candidateSkills == null || jdSkills == null) {
            return BigDecimal.ZERO;
        }

        Set<String> candidateSet = new HashSet<>(Arrays.asList(candidateSkills.toLowerCase().split(",")));
        Set<String> jdSet = new HashSet<>(Arrays.asList(jdSkills.toLowerCase().split(",")));

        // 计算交集
        Set<String> intersection = new HashSet<>(candidateSet);
        intersection.retainAll(jdSet);

        // 计算并集
        Set<String> union = new HashSet<>(candidateSet);
        union.addAll(jdSet);

        // Jaccard相似度 = 交集 / 并集
        double jaccard = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
        return BigDecimal.valueOf(jaccard * 100).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 经验匹配分
     */
    private BigDecimal calculateExperienceScore(Integer candidateExp, Integer jdExp) {
        if (candidateExp == null || jdExp == null) {
            return new BigDecimal("50");
        }

        if (candidateExp >= jdExp) {
            // 经验充足，满分
            return new BigDecimal("100");
        } else {
            // 经验不足，按比例扣分
            double ratio = (double) candidateExp / jdExp;
            return BigDecimal.valueOf(ratio * 100).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 学历匹配分
     */
    private BigDecimal calculateEducationScore(String candidateEdu, String jdEdu) {
        Map<String, Integer> eduLevel = Map.of(
                "博士", 5,
                "硕士", 4,
                "本科", 3,
                "大专", 2,
                "高中", 1
        );

        Integer candidateLevel = eduLevel.getOrDefault(candidateEdu, 0);
        Integer jdLevel = eduLevel.getOrDefault(jdEdu, 0);

        if (candidateLevel >= jdLevel) {
            return new BigDecimal("100");
        } else {
            return new BigDecimal("50"); // 学历不足，给50分
        }
    }

    /**
     * 薪资匹配分
     */
    private BigDecimal calculateSalaryScore(Integer candidateSalary, String jdSalaryRange) {
        if (candidateSalary == null || jdSalaryRange == null) {
            return new BigDecimal("80");
        }

        // 解析薪资范围（如"20k-35k"）
        String[] parts = jdSalaryRange.toLowerCase().replace("k", "").split("-");
        if (parts.length != 2) {
            return new BigDecimal("80");
        }

        try {
            int minSalary = Integer.parseInt(parts[0].trim());
            int maxSalary = Integer.parseInt(parts[1].trim());
            int midSalary = (minSalary + maxSalary) / 2;

            // 期望薪资在范围内，满分
            if (candidateSalary >= minSalary && candidateSalary <= maxSalary) {
                return new BigDecimal("100");
            }

            // 期望薪资接近范围，给高分
            double diff = Math.abs(candidateSalary - midSalary);
            double range = maxSalary - minSalary;
            double score = Math.max(0, 100 - (diff / range * 50));
            return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            return new BigDecimal("80");
        }
    }

    /**
     * 地点匹配分
     */
    private BigDecimal calculateLocationScore(String candidateCity, String jdLocation) {
        if (candidateCity == null || jdLocation == null) {
            return new BigDecimal("80");
        }

        // 简化版：完全匹配100分，否则50分
        if (jdLocation.contains(candidateCity) || candidateCity.contains(jdLocation)) {
            return new BigDecimal("100");
        } else {
            return new BigDecimal("50");
        }
    }

    /**
     * 计算总分（加权平均）
     */
    private BigDecimal calculateTotalScore(CandidateScoringResult.DimensionScores scores, Map<String, BigDecimal> weights) {
        BigDecimal total = BigDecimal.ZERO;
        total = total.add(scores.getSkillScore().multiply(weights.get("skill")));
        total = total.add(scores.getExperienceScore().multiply(weights.get("experience")));
        total = total.add(scores.getEducationScore().multiply(weights.get("education")));
        total = total.add(scores.getProjectScore().multiply(weights.get("project")));
        total = total.add(scores.getSalaryScore().multiply(weights.get("salary")));
        total = total.add(scores.getStabilityScore().multiply(weights.get("stability")));
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 使用LLM生成可解释性内容
     */
    private ExplainabilityResult generateExplainability(CandidateResumeDocument candidate, JobDescription jd,
                                                        CandidateScoringResult.DimensionScores scores, BigDecimal totalScore) {
        try {
            String prompt = buildExplainabilityPrompt(candidate, jd, scores, totalScore);
            String llmResponse = callLLM(prompt);
            return parseExplainability(llmResponse);

        } catch (Exception e) {
            log.error("生成可解释性内容失败", e);
            // 返回默认内容
            return new ExplainabilityResult(
                    "候选人综合匹配度为" + totalScore + "分",
                    "暂无风险分析",
                    Arrays.asList("技能匹配度" + scores.getSkillScore() + "分"),
                    Arrays.asList(),
                    "综合评分" + totalScore + "分"
            );
        }
    }

    /**
     * 构建可解释性Prompt
     */
    private String buildExplainabilityPrompt(CandidateResumeDocument candidate, JobDescription jd,
                                             CandidateScoringResult.DimensionScores scores, BigDecimal totalScore) {
        return String.format(
                "你是一名资深招聘专家，请对以下候选人的匹配情况进行详细分析。\n\n" +
                        "【岗位要求】\n%s\n\n" +
                        "【候选人信息】\n姓名：%s\n技能：%s\n经验：%d年\n学历：%s\n期望薪资：%dK\n\n" +
                        "【评分结果】\n总分：%.2f\n技能匹配：%.2f\n经验匹配：%.2f\n学历匹配：%.2f\n\n" +
                        "请严格按照JSON格式输出分析结果：\n" +
                        "{\n" +
                        "  \"matchReason\": \"匹配理由（100字以内）\",\n" +
                        "  \"riskAnalysis\": \"风险分析（100字以内）\",\n" +
                        "  \"advantages\": [\"优势1\", \"优势2\", \"优势3\"],\n" +
                        "  \"disadvantages\": [\"劣势1\", \"劣势2\"],\n" +
                        "  \"recommendationReason\": \"推荐理由（50字以内）\"\n" +
                        "}",
                jd.getTitle() + "\n" + jd.getRequirements(),
                candidate.getName(), candidate.getSkills(), candidate.getExperience(), candidate.getEducation(), candidate.getExpectedSalary(),
                totalScore, scores.getSkillScore(), scores.getExperienceScore(), scores.getEducationScore()
        );
    }

    /**
     * 调用LLM
     */
    private String callLLM(String prompt) throws Exception {
        Generation gen = new Generation();
        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(modelName)
                .messages(List.of(Message.builder().role(Role.USER.getValue()).content(prompt).build()))
                .temperature(0.3f)
                .maxTokens(800)
                .build();

        GenerationResult result = gen.call(param);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    /**
     * 解析可解释性结果
     */
    private ExplainabilityResult parseExplainability(String llmResponse) {
        String jsonStr = llmResponse.trim();
        if (jsonStr.startsWith("```json")) jsonStr = jsonStr.substring(7);
        if (jsonStr.startsWith("```")) jsonStr = jsonStr.substring(3);
        if (jsonStr.endsWith("```")) jsonStr = jsonStr.substring(0, jsonStr.length() - 3);

        JSONObject json = JSON.parseObject(jsonStr.trim());
        return new ExplainabilityResult(
                json.getString("matchReason"),
                json.getString("riskAnalysis"),
                json.getObject("advantages", List.class),
                json.getObject("disadvantages", List.class),
                json.getString("recommendationReason")
        );
    }

    /**
     * 确定推荐度
     */
    private String determineRecommendation(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal("85")) >= 0) {
            return "highly_recommend";
        } else if (totalScore.compareTo(new BigDecimal("70")) >= 0) {
            return "recommend";
        } else if (totalScore.compareTo(new BigDecimal("50")) >= 0) {
            return "consider";
        } else {
            return "not_recommend";
        }
    }

    /**
     * 保存评分记录
     */
    private void saveScoringRecord(CandidateScoringResult result, CandidateScoringResult.DimensionScores scores) {
        CandidateScore record = new CandidateScore();
        record.setCandidateId(result.getCandidateId());
        record.setJdId(result.getJdId());
        record.setTotalScore(result.getTotalScore());
        record.setSkillScore(scores.getSkillScore());
        record.setExperienceScore(scores.getExperienceScore());
        record.setEducationScore(scores.getEducationScore());
        record.setSalaryScore(scores.getSalaryScore());
        record.setLocationScore(scores.getLocationScore());
        record.setProjectScore(scores.getProjectScore());
        record.setStabilityScore(scores.getStabilityScore());
        record.setMatchReason(result.getMatchReason());
        record.setRiskAnalysis(result.getRiskAnalysis());
        record.setAdvantages(JSON.toJSONString(result.getAdvantages()));
        record.setDisadvantages(JSON.toJSONString(result.getDisadvantages()));
        record.setRecommendation(result.getRecommendation());
        record.setScoringVersion("v1.0");
        record.setCreatedAt(new Date());
        record.setUpdatedAt(new Date());

        candidateScoreMapper.insert(record);
    }

    /**
     * 可解释性结果
     */
    private static class ExplainabilityResult {
        private final String matchReason;
        private final String riskAnalysis;
        private final List<String> advantages;
        private final List<String> disadvantages;
        private final String recommendationReason;

        public ExplainabilityResult(String matchReason, String riskAnalysis, List<String> advantages,
                                    List<String> disadvantages, String recommendationReason) {
            this.matchReason = matchReason;
            this.riskAnalysis = riskAnalysis;
            this.advantages = advantages;
            this.disadvantages = disadvantages;
            this.recommendationReason = recommendationReason;
        }

        public String getMatchReason() { return matchReason; }
        public String getRiskAnalysis() { return riskAnalysis; }
        public List<String> getAdvantages() { return advantages; }
        public List<String> getDisadvantages() { return disadvantages; }
        public String getRecommendationReason() { return recommendationReason; }
    }
}
