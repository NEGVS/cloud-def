package xCloud.service.recruitment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import xCloud.dto.recruitment.CandidateScoringResult;
import xCloud.entity.es.CandidateResumeDocument;
import xCloud.entity.recruitment.CandidateScore;
import xCloud.entity.recruitment.JobDescription;
import xCloud.mapper.recruitment.CandidateScoreMapper;
import xCloud.mapper.recruitment.JobDescriptionMapper;
import xCloud.repository.CandidateResumeRepository;
import xCloud.util.LlmJsonUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 候选人评分引擎
 * 核心功能：多维度评分 + 可解释性 + HR偏好驱动
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class CandidateScoringService {

    @Resource
    private CandidateScoreMapper candidateScoreMapper;

    @Resource
    private JobDescriptionMapper jobDescriptionMapper;

    @Resource
    private CandidateResumeRepository candidateResumeRepository;

    /**
     * 事务模板：仅包裹评分记录落库这一条写操作，LLM 可解释性生成在事务外
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RecruitmentLlmHelper llmHelper;

    /**
     * 多维度权重配置
     * <p>
     * ============权重之和必须 = 1.00，且每个已计算维度都必须有对应权重============
     * 历史上 location 维度被计算后未纳入总分（漏配权重），导致该维度形同虚设。
     * 任何新增维度必须同时在此登记权重，否则会被静默丢弃。
     */
    private static final Map<String, BigDecimal> DEFAULT_WEIGHTS = Map.of(
            "skill", new BigDecimal("0.30"),
            "experience", new BigDecimal("0.25"),
            "education", new BigDecimal("0.15"),
            "project", new BigDecimal("0.10"),
            "salary", new BigDecimal("0.10"),
            "stability", new BigDecimal("0.05"),
            "location", new BigDecimal("0.05")
    );

    /**
     * 对候选人进行评分
     * <p>
     * 事务边界：读取候选人/JD、纯本地打分、LLM 可解释性生成均在事务【外】；
     * 仅最后的评分记录 insert 走短事务，避免 LLM 期间占用数据库连接。
     *
     * @param candidateId 候选人ID
     * @param jdId JD ID
     * @return 评分结果
     */
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

            // ============2-计算各维度分数（纯本地计算）============
            CandidateScoringResult.DimensionScores dimensionScores = calculateDimensionScores(candidate, jd);

            // ============3-计算总分（加权平均）============
            BigDecimal totalScore = calculateTotalScore(dimensionScores, DEFAULT_WEIGHTS);

            // ============4-使用LLM生成可解释性内容（事务外，耗时操作）============
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

            // ============6-短事务保存评分记录============
            transactionTemplate.executeWithoutResult(status -> saveScoringRecord(result, dimensionScores));

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
     * <p>
     * 两侧格式不同，必须分别归一化：
     * - 候选人侧 {@code Resume.skills} 存的是 JSON 数组字符串，如 {@code ["Java","Spring Boot"]}
     * - JD 侧 {@code skillTags} 是逗号分隔字符串，如 {@code Java,Spring Boot,MySQL}
     * 统一 trim + 转小写 + 去空，避免 " Java" 与 "Java" 不相等、空串膨胀并集。
     */
    private BigDecimal calculateSkillScore(String candidateSkills, String jdSkills) {
        Set<String> candidateSet = tokenizeSkills(candidateSkills);
        Set<String> jdSet = tokenizeSkills(jdSkills);
        if (candidateSet.isEmpty() || jdSet.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算交集
        Set<String> intersection = new HashSet<>(candidateSet);
        intersection.retainAll(jdSet);

        // 计算并集
        Set<String> union = new HashSet<>(candidateSet);
        union.addAll(jdSet);

        // Jaccard相似度 = 交集 / 并集
        double jaccard = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
        return scaled(jaccard * 100);
    }

    /**
     * 技能归一化：兼容 JSON 数组字符串 与 逗号分隔字符串
     *
     * @param raw 原始技能串
     * @return 归一化后的技能集合（小写、去空白、去空串）
     */
    private Set<String> tokenizeSkills(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptySet();
        }
        String trimmed = raw.trim();
        List<String> parts;
        if (trimmed.startsWith("[")) {
            // 候选人侧：JSON 数组字符串，解析失败降级为逗号切分
            try {
                parts = JSON.parseArray(trimmed, String.class);
            } catch (Exception e) {
                parts = Arrays.asList(trimmed.substring(1, Math.max(1, trimmed.length() - 1)).split(","));
            }
        } else {
            parts = Arrays.asList(trimmed.split(","));
        }
        return parts.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * 经验匹配分
     */
    private BigDecimal calculateExperienceScore(Integer candidateExp, Integer jdExp) {
        if (candidateExp == null || jdExp == null) {
            return new BigDecimal("50");
        }
        // ============除零保护：JD未要求经验视为满分============
        if (jdExp <= 0) {
            return new BigDecimal("100");
        }

        if (candidateExp >= jdExp) {
            // 经验充足，满分
            return new BigDecimal("100");
        } else {
            // 经验不足，按比例扣分
            double ratio = (double) candidateExp / jdExp;
            return scaled(ratio * 100);
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
     * <p>
     * ============除零保护：当 min==max（薪资区间为单点）时 range=0，不能做除法============
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

            // 期望薪资在范围内，满分
            if (candidateSalary >= minSalary && candidateSalary <= maxSalary) {
                return new BigDecimal("100");
            }

            int midSalary = (minSalary + maxSalary) / 2;
            double diff = Math.abs(candidateSalary - midSalary);
            double range = maxSalary - minSalary;

            // range=0 无法按区间比例衰减，改用绝对差值衰减（每超出 1K 扣 10 分）
            if (range <= 0) {
                return scaled(Math.max(0, 100 - diff * 10));
            }

            double score = Math.max(0, 100 - (diff / range * 50));
            return scaled(score);

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
     * <p>
     * ============按权重表逐项取分，避免已计算维度被漏算============
     * 通过与 DEFAULT_WEIGHTS 的 key 对应取值，新增维度只要登记了权重就会自动纳入总分，
     * 不再依赖手写 6 行加法（历史上 location 就是这样被漏掉的）。
     */
    private BigDecimal calculateTotalScore(CandidateScoringResult.DimensionScores scores, Map<String, BigDecimal> weights) {
        Map<String, BigDecimal> dimensionMap = Map.of(
                "skill", nz(scores.getSkillScore()),
                "experience", nz(scores.getExperienceScore()),
                "education", nz(scores.getEducationScore()),
                "project", nz(scores.getProjectScore()),
                "salary", nz(scores.getSalaryScore()),
                "stability", nz(scores.getStabilityScore()),
                "location", nz(scores.getLocationScore())
        );

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> weightEntry : weights.entrySet()) {
            BigDecimal dimensionScore = dimensionMap.get(weightEntry.getKey());
            if (dimensionScore == null) {
                log.warn("权重配置了未知维度 [{}]，已跳过，请检查 DEFAULT_WEIGHTS 与 DimensionScores 是否一致", weightEntry.getKey());
                continue;
            }
            total = total.add(dimensionScore.multiply(weightEntry.getValue()));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 空分兜底为 0，避免 NPE
     */
    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * double → BigDecimal 安全转换：先挡掉 NaN/Infinity（BigDecimal.valueOf 会抛 NumberFormatException）
     */
    private BigDecimal scaled(double value) {
        if (!Double.isFinite(value)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 使用LLM生成可解释性内容
     */
    private ExplainabilityResult generateExplainability(CandidateResumeDocument candidate, JobDescription jd,
                                                        CandidateScoringResult.DimensionScores scores, BigDecimal totalScore) {
        try {
            String prompt = buildExplainabilityPrompt(candidate, jd, scores, totalScore);
            // 事务外调用，避免 LLM 期间占用数据库连接
            String llmResponse = llmHelper.syncChat(prompt, 0.3f, 800);
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
     * 解析可解释性结果
     */
    private ExplainabilityResult parseExplainability(String llmResponse) {
        JSONObject json = JSON.parseObject(LlmJsonUtil.extractJson(llmResponse));
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
