package xCloud.service.recruitment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import xCloud.dto.recruitment.JDFeedbackRequest;
import xCloud.dto.recruitment.JDGenerationRequest;
import xCloud.entity.recruitment.JDFeedback;
import xCloud.entity.recruitment.JobDescription;
import xCloud.mapper.recruitment.JDFeedbackMapper;
import xCloud.mapper.recruitment.JobDescriptionMapper;
import xCloud.util.LlmJsonUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JD生成与优化服务
 * 核心功能：
 * 1. 理解HR需求 → 生成JD
 * 2. HR反馈 → 迭代优化
 * 3. 多轮对话式生成
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class JDGenerationService {

    @Resource
    private JobDescriptionMapper jobDescriptionMapper;

    @Resource
    private JDFeedbackMapper jdFeedbackMapper;

    /**
     * 事务模板：仅包裹数据库写入，LLM 调用一律在事务外，避免长事务占用连接
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RecruitmentLlmHelper llmHelper;

    /**
     * JD 状态：草稿
     */
    private static final String STATUS_DRAFT = "draft";

    /**
     * 反馈类型：调整优化
     */
    private static final String FEEDBACK_ADJUST = "adjust";

    /**
     * 反馈类型：通过
     */
    private static final String FEEDBACK_APPROVE = "approve";

    /**
     * 反馈类型：拒绝
     */
    private static final String FEEDBACK_REJECT = "reject";

    /**
     * 生成JD（第一版）
     * <p>
     * 仅一次 insert，无需事务；LLM 调用在事务外执行。
     *
     * @param request 生成请求
     * @return 生成的JD
     */
    public JobDescription generateJD(JDGenerationRequest request) {
        try {
            log.info("开始生成JD：title={}, strategy={}", request.getTitle(), request.getGenerationStrategy());

            // ============1-构建生成Prompt============
            String prompt = buildGenerationPrompt(request);

            // ============2-调用LLM生成（事务外，耗时操作）============
            String llmResponse = llmHelper.syncChat(prompt, 0.7f, 2000);

            // ============3-解析LLM返回的结构化JD============
            JobDescription jd = parseJDFromLLM(llmResponse, request);

            // ============4-短事务保存到数据库============
            jd.setCreatedAt(new Date());
            jd.setUpdatedAt(new Date());
            jd.setVersion(1);
            jd.setStatus(STATUS_DRAFT);
            jd.setDeleted(0);
            transactionTemplate.executeWithoutResult(status -> jobDescriptionMapper.insert(jd));

            log.info("✅ JD生成成功：id={}, version={}", jd.getId(), jd.getVersion());
            return jd;

        } catch (Exception e) {
            log.error("JD生成失败：title={}", request.getTitle(), e);
            throw new RuntimeException("JD生成失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据HR反馈优化JD（生成新版本）
     * <p>
     * 事务边界：
     * - adjust：LLM 生成新版本放在事务【外】，随后一个短事务内【插入反馈记录 + 插入新版本JD】；
     * - approve/reject：一个短事务内【插入反馈记录 + 更新JD状态】。
     * 全程不把耗时 LLM 调用包进事务。
     *
     * @param feedbackRequest 反馈请求
     * @return 优化/发布/归档后的JD
     */
    public JobDescription optimizeJD(JDFeedbackRequest feedbackRequest) {
        Long jdId = feedbackRequest.getJdId();
        String feedbackType = feedbackRequest.getFeedbackType();
        try {
            log.info("开始优化JD：jdId={}, version={}, feedbackType={}",
                    jdId, feedbackRequest.getJdVersion(), feedbackType);

            // ============1-参数校验：反馈类型白名单，非法值不再落到 adjust 分支============
            if (!FEEDBACK_ADJUST.equals(feedbackType) && !FEEDBACK_APPROVE.equals(feedbackType)
                    && !FEEDBACK_REJECT.equals(feedbackType)) {
                throw new IllegalArgumentException("非法的反馈类型：" + feedbackType + "，仅支持 adjust/approve/reject");
            }

            // ============2-只加载一次JD并判空，避免 NPE============
            JobDescription oldJD = jobDescriptionMapper.selectById(jdId);
            if (oldJD == null) {
                throw new IllegalArgumentException("JD不存在：" + jdId);
            }

            // ============3-构建反馈实体============
            JDFeedback feedback = new JDFeedback();
            feedback.setJdId(jdId);
            feedback.setJdVersion(feedbackRequest.getJdVersion());
            feedback.setFeedbackType(feedbackType);
            feedback.setContent(feedbackRequest.getContent());
            feedback.setAdjustFields(feedbackRequest.getAdjustFields());
            feedback.setHrUserId(feedbackRequest.getHrUserId());
            feedback.setCreatedAt(new Date());

            // ============4-approve：发布============
            if (FEEDBACK_APPROVE.equals(feedbackType)) {
                return transactionTemplate.execute(status -> {
                    jdFeedbackMapper.insert(feedback);
                    oldJD.setStatus("published");
                    oldJD.setPublishedAt(new Date());
                    oldJD.setUpdatedAt(new Date());
                    jobDescriptionMapper.updateById(oldJD);
                    return oldJD;
                });
            }

            // ============5-reject：归档============
            if (FEEDBACK_REJECT.equals(feedbackType)) {
                return transactionTemplate.execute(status -> {
                    jdFeedbackMapper.insert(feedback);
                    oldJD.setStatus("archived");
                    oldJD.setUpdatedAt(new Date());
                    jobDescriptionMapper.updateById(oldJD);
                    return oldJD;
                });
            }

            // ============6-adjust：LLM 生成新版本（事务外）============
            String optimizationPrompt = buildOptimizationPrompt(oldJD, feedbackRequest);
            String llmResponse = llmHelper.syncChat(optimizationPrompt, 0.7f, 2000);
            JobDescription newJD = parseOptimizedJD(llmResponse, oldJD, feedbackRequest);

            newJD.setId(null); // 新记录
            newJD.setVersion(oldJD.getVersion() + 1);
            newJD.setStatus(STATUS_DRAFT);
            newJD.setCreatedAt(new Date());
            newJD.setUpdatedAt(new Date());
            newJD.setDeleted(0);

            // ============7-短事务：插入反馈记录 + 插入新版本============
            JobDescription saved = transactionTemplate.execute(status -> {
                jdFeedbackMapper.insert(feedback);
                jobDescriptionMapper.insert(newJD);
                return newJD;
            });

            log.info("✅ JD优化成功：id={}, oldVersion={}, newVersion={}",
                    saved.getId(), oldJD.getVersion(), saved.getVersion());
            return saved;

        } catch (Exception e) {
            log.error("JD优化失败：jdId={}", jdId, e);
            throw new RuntimeException("JD优化失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建JD生成Prompt
     */
    private String buildGenerationPrompt(JDGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名专业的招聘专家和HR文案撰写者，请根据以下需求生成一份完整的岗位描述（JD）。\n\n");
        prompt.append("【岗位信息】\n");
        prompt.append("岗位标题：").append(request.getTitle()).append("\n");
        prompt.append("需求描述：").append(request.getRequirement()).append("\n");
        if (request.getSalaryRange() != null) {
            prompt.append("薪资范围：").append(request.getSalaryRange()).append("\n");
        }
        if (request.getLocation() != null) {
            prompt.append("工作地点：").append(request.getLocation()).append("\n");
        }
        if (request.getEducation() != null) {
            prompt.append("学历要求：").append(request.getEducation()).append("\n");
        }
        if (request.getExperienceYears() != null) {
            prompt.append("工作经验：").append(request.getExperienceYears()).append("年\n");
        }
        if (request.getCompanyIntro() != null) {
            prompt.append("公司介绍：").append(request.getCompanyIntro()).append("\n");
        }

        prompt.append("\n【生成策略】").append(getStrategyDescription(request.getGenerationStrategy())).append("\n");

        prompt.append("\n【输出要求】\n");
        prompt.append("请严格按照以下JSON格式输出：\n");
        prompt.append("{\n");
        prompt.append("  \"responsibilities\": \"岗位职责（3-5条，每条一行）\",\n");
        prompt.append("  \"requirements\": \"任职要求（5-8条，每条一行）\",\n");
        prompt.append("  \"skillTags\": \"技能标签（逗号分隔，如：Java,Spring Boot,MySQL）\",\n");
        prompt.append("  \"highlights\": \"岗位亮点/福利（3-5条）\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * 构建JD优化Prompt
     */
    private String buildOptimizationPrompt(JobDescription oldJD, JDFeedbackRequest feedback) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名专业的招聘专家，请根据HR的反馈优化以下JD。\n\n");
        prompt.append("【当前JD（版本").append(oldJD.getVersion()).append("）】\n");
        prompt.append("岗位标题：").append(oldJD.getTitle()).append("\n");
        prompt.append("岗位职责：\n").append(oldJD.getResponsibilities()).append("\n\n");
        prompt.append("任职要求：\n").append(oldJD.getRequirements()).append("\n\n");
        prompt.append("技能标签：").append(oldJD.getSkillTags()).append("\n");
        prompt.append("岗位亮点：\n").append(oldJD.getHighlights()).append("\n\n");

        prompt.append("【HR反馈】\n");
        prompt.append(feedback.getContent()).append("\n\n");
        prompt.append("需要调整的字段：").append(feedback.getAdjustFields()).append("\n\n");

        prompt.append("【优化要求】\n");
        prompt.append("1. 仔细阅读HR的反馈意见\n");
        prompt.append("2. 针对性地优化指定字段\n");
        prompt.append("3. 保持其他字段不变\n");
        prompt.append("4. 使JD更符合HR的期望\n\n");

        prompt.append("请严格按照以下JSON格式输出优化后的完整JD：\n");
        prompt.append("{\n");
        prompt.append("  \"responsibilities\": \"岗位职责\",\n");
        prompt.append("  \"requirements\": \"任职要求\",\n");
        prompt.append("  \"skillTags\": \"技能标签\",\n");
        prompt.append("  \"highlights\": \"岗位亮点\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * 解析LLM返回的JD（首次生成）
     */
    private JobDescription parseJDFromLLM(String llmResponse, JDGenerationRequest request) {
        try {
            // ============提取JSON部分（LLM可能返回带解释的文本）============
            String jsonStr = LlmJsonUtil.extractJson(llmResponse);
            JSONObject json = JSON.parseObject(jsonStr);

            JobDescription jd = new JobDescription();
            jd.setTitle(request.getTitle());
            jd.setResponsibilities(json.getString("responsibilities"));
            jd.setRequirements(json.getString("requirements"));
            jd.setSkillTags(json.getString("skillTags"));
            jd.setHighlights(json.getString("highlights"));
            jd.setSalaryRange(request.getSalaryRange());
            jd.setLocation(request.getLocation());
            jd.setEducation(request.getEducation());
            jd.setExperienceYears(request.getExperienceYears());
            jd.setCompanyIntro(request.getCompanyIntro());
            jd.setOriginalRequirement(request.getRequirement());
            jd.setGenerationStrategy(request.getGenerationStrategy());
            jd.setCreatorId(request.getHrUserId());

            return jd;

        } catch (Exception e) {
            log.error("JD解析失败：llmResponse={}", llmResponse, e);
            throw new RuntimeException("JD解析失败", e);
        }
    }

    /**
     * 解析优化后的JD
     */
    private JobDescription parseOptimizedJD(String llmResponse, JobDescription oldJD, JDFeedbackRequest feedback) {
        try {
            String jsonStr = LlmJsonUtil.extractJson(llmResponse);
            JSONObject json = JSON.parseObject(jsonStr);

            JobDescription newJD = new JobDescription();
            newJD.setTitle(oldJD.getTitle());
            newJD.setResponsibilities(json.getString("responsibilities"));
            newJD.setRequirements(json.getString("requirements"));
            newJD.setSkillTags(json.getString("skillTags"));
            newJD.setHighlights(json.getString("highlights"));
            newJD.setSalaryRange(oldJD.getSalaryRange());
            newJD.setLocation(oldJD.getLocation());
            newJD.setEducation(oldJD.getEducation());
            newJD.setExperienceYears(oldJD.getExperienceYears());
            newJD.setCompanyIntro(oldJD.getCompanyIntro());
            newJD.setOriginalRequirement(oldJD.getOriginalRequirement());
            newJD.setGenerationStrategy(oldJD.getGenerationStrategy());
            newJD.setCreatorId(feedback.getHrUserId());

            // ============记录反馈历史============
            List<String> feedbackHistory = new ArrayList<>();
            if (oldJD.getFeedbackHistory() != null) {
                feedbackHistory = JSON.parseArray(oldJD.getFeedbackHistory(), String.class);
            }
            feedbackHistory.add("v" + oldJD.getVersion() + ": " + feedback.getContent());
            newJD.setFeedbackHistory(JSON.toJSONString(feedbackHistory));

            return newJD;

        } catch (Exception e) {
            log.error("优化JD解析失败：llmResponse={}", llmResponse, e);
            throw new RuntimeException("优化JD解析失败", e);
        }
    }

    /**
     * 获取生成策略描述
     */
    private String getStrategyDescription(String strategy) {
        return switch (strategy) {
            case "attractive" -> "吸引人风格：文案生动活泼，突出岗位亮点和发展机会，吸引候选人投递";
            case "concise" -> "简洁风格：言简意赅，核心要点清晰，适合快速阅读";
            default -> "标准风格：专业、规范、全面，适合大多数企业岗位";
        };
    }
}
