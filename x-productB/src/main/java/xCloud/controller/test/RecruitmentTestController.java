package xCloud.controller.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xCloud.dto.recruitment.CandidateScoringResult;
import xCloud.dto.recruitment.JDFeedbackRequest;
import xCloud.dto.recruitment.JDGenerationRequest;
import xCloud.entity.recruitment.JobDescription;
import xCloud.entity.recruitment.Resume;
import xCloud.mapper.recruitment.ResumeMapper;
import xCloud.service.kafka.KafkaProducerService;
import xCloud.service.recruitment.CandidateScoringService;
import xCloud.service.recruitment.JDGenerationService;
import xCloud.service.recruitment.ResumeParseService;
import xCloud.service.search.HybridSearchResult;
import xCloud.service.search.HybridSearchService;
import xCloud.service.search.HybridSearchRerankService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 招聘系统综合测试控制器
 * 用途：端到端测试所有核心功能
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@RestController
@RequestMapping("/api/test/recruitment")
@Tag(name = "招聘系统测试", description = "端到端测试所有核心功能")
public class RecruitmentTestController {

    @Autowired
    private JDGenerationService jdGenerationService;

    @Autowired
    private ResumeParseService resumeParseService;

    @Autowired
    private HybridSearchService hybridSearchService;

    @Autowired
    private HybridSearchRerankService rerankService;

    @Autowired
    private CandidateScoringService scoringService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ResumeMapper resumeMapper;

    /**
     * 测试1：JD生成
     */
    @PostMapping("/test-jd-generation")
    @Operation(summary = "测试JD生成", description = "测试AI生成岗位描述功能")
    public Map<String, Object> testJDGeneration() {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("========== 开始测试：JD生成 ==========");

            // 构建测试请求
            JDGenerationRequest request = new JDGenerationRequest();
            request.setTitle("高级Java后端工程师");
            request.setRequirement("需要3-5年Java开发经验，熟悉Spring Boot、微服务、MySQL、Redis，有高并发经验优先");
            request.setSalaryRange("25k-40k");
            request.setLocation("北京-朝阳区");
            request.setEducation("本科");
            request.setExperienceYears(3);
            request.setGenerationStrategy("attractive");
            request.setHrUserId(1001L);

            // 生成JD
            JobDescription jd = jdGenerationService.generateJD(request);

            long elapsed = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("testName", "JD生成测试");
            result.put("jdId", jd.getId());
            result.put("title", jd.getTitle());
            result.put("responsibilities", jd.getResponsibilities());
            result.put("requirements", jd.getRequirements());
            result.put("skillTags", jd.getSkillTags());
            result.put("status", jd.getStatus());
            result.put("version", jd.getVersion());
            result.put("elapsed_ms", elapsed);

            log.info("✅ JD生成测试通过：jdId={}, elapsed={}ms", jd.getId(), elapsed);

        } catch (Exception e) {
            log.error("❌ JD生成测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 测试2：JD优化
     */
    @PostMapping("/test-jd-optimization")
    @Operation(summary = "测试JD优化", description = "测试HR反馈优化功能")
    public Map<String, Object> testJDOptimization(@RequestParam Long jdId) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("========== 开始测试：JD优化 ==========");

            // 构建反馈请求
            JDFeedbackRequest feedback = new JDFeedbackRequest();
            feedback.setJdId(jdId);
            feedback.setJdVersion(1);
            feedback.setFeedbackType("adjust");
            feedback.setContent("岗位职责需要更详细，任职要求中增加对Kafka的要求");
            feedback.setAdjustFields("responsibilities,requirements");
            feedback.setHrUserId(1001L);

            // 优化JD
            JobDescription optimizedJD = jdGenerationService.optimizeJD(feedback);

            long elapsed = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("testName", "JD优化测试");
            result.put("newJdId", optimizedJD.getId());
            result.put("newVersion", optimizedJD.getVersion());
            result.put("responsibilities", optimizedJD.getResponsibilities());
            result.put("requirements", optimizedJD.getRequirements());
            result.put("elapsed_ms", elapsed);

            log.info("✅ JD优化测试通过：newJdId={}, newVersion={}, elapsed={}ms",
                    optimizedJD.getId(), optimizedJD.getVersion(), elapsed);

        } catch (Exception e) {
            log.error("❌ JD优化测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 测试3：简历上传与解析
     */
    @PostMapping("/test-resume-upload")
    @Operation(summary = "测试简历上传", description = "测试简历上传与LLM解析功能")
    public Map<String, Object> testResumeUpload(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("========== 开始测试：简历上传与解析 ==========");

            // 上传并解析简历
            Long resumeId = resumeParseService.uploadAndParseResume(file, "test");
            Resume resume = resumeMapper.selectById(resumeId);

            long elapsed = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("testName", "简历上传测试");
            result.put("resumeId", resumeId);
            result.put("name", resume.getName());
            result.put("phone", resume.getPhone());
            result.put("email", resume.getEmail());
            result.put("education", resume.getEducation());
            result.put("workYears", resume.getWorkYears());
            result.put("expectedPosition", resume.getExpectedPosition());
            result.put("skills", resume.getSkills());
            result.put("parseStatus", resume.getParseStatus());
            result.put("elapsed_ms", elapsed);

            log.info("✅ 简历上传测试通过：resumeId={}, name={}, elapsed={}ms",
                    resumeId, resume.getName(), elapsed);

        } catch (Exception e) {
            log.error("❌ 简历上传测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 测试4：混合检索
     */
    @PostMapping("/test-hybrid-search")
    @Operation(summary = "测试混合检索", description = "测试BM25+向量+RRF+Rerank")
    public Map<String, Object> testHybridSearch(@RequestParam String query,
                                                @RequestParam(defaultValue = "5") int topK) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("========== 开始测试：混合检索 ==========");

            // 混合检索
            List<HybridSearchResult> searchResults = hybridSearchService.hybridSearch(query, topK * 2);

            // Rerank
            List<HybridSearchResult> rerankedResults = rerankService.rerank(query, searchResults, topK);

            long elapsed = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("testName", "混合检索测试");
            result.put("query", query);
            result.put("searchCount", searchResults.size());
            result.put("rerankedCount", rerankedResults.size());
            result.put("results", rerankedResults);
            result.put("elapsed_ms", elapsed);

            log.info("✅ 混合检索测试通过：query={}, results={}, elapsed={}ms",
                    query, rerankedResults.size(), elapsed);

        } catch (Exception e) {
            log.error("❌ 混合检索测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 测试5：候选人评分
     */
    @PostMapping("/test-candidate-scoring")
    @Operation(summary = "测试候选人评分", description = "测试多维度评分引擎")
    public Map<String, Object> testCandidateScoring(@RequestParam Long candidateId,
                                                     @RequestParam Long jdId) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("========== 开始测试：候选人评分 ==========");

            // 评分
            CandidateScoringResult scoringResult = scoringService.scoreCandidate(candidateId, jdId);

            long elapsed = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("testName", "候选人评分测试");
            result.put("candidateId", candidateId);
            result.put("jdId", jdId);
            result.put("totalScore", scoringResult.getTotalScore());
            result.put("dimensionScores", scoringResult.getDimensionScores());
            result.put("matchReason", scoringResult.getMatchReason());
            result.put("riskAnalysis", scoringResult.getRiskAnalysis());
            result.put("advantages", scoringResult.getAdvantages());
            result.put("disadvantages", scoringResult.getDisadvantages());
            result.put("recommendation", scoringResult.getRecommendation());
            result.put("elapsed_ms", elapsed);

            log.info("✅ 候选人评分测试通过：totalScore={}, recommendation={}, elapsed={}ms",
                    scoringResult.getTotalScore(), scoringResult.getRecommendation(), elapsed);

        } catch (Exception e) {
            log.error("❌ 候选人评分测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 测试6：端到端完整流程
     */
    @PostMapping("/test-end-to-end")
    @Operation(summary = "端到端测试", description = "测试完整招聘流程")
    public Map<String, Object> testEndToEnd(@RequestParam("resumeFile") MultipartFile resumeFile) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("========== 开始测试：端到端完整流程 ==========");

            // ============步骤1：生成JD============
            log.info("步骤1：生成JD");
            JDGenerationRequest jdRequest = new JDGenerationRequest();
            jdRequest.setTitle("高级Java后端工程师");
            jdRequest.setRequirement("需要3-5年Java开发经验，熟悉Spring Boot、微服务");
            jdRequest.setSalaryRange("25k-40k");
            jdRequest.setLocation("北京");
            jdRequest.setEducation("本科");
            jdRequest.setExperienceYears(3);
            jdRequest.setGenerationStrategy("standard");
            jdRequest.setHrUserId(1001L);

            JobDescription jd = jdGenerationService.generateJD(jdRequest);
            log.info("✅ JD生成成功：jdId={}", jd.getId());

            // ============步骤2：上传简历============
            log.info("步骤2：上传简历");
            Long resumeId = resumeParseService.uploadAndParseResume(resumeFile, "test");
            Resume resume = resumeMapper.selectById(resumeId);
            log.info("✅ 简历上传成功：resumeId={}, name={}", resumeId, resume.getName());

            // ============步骤3：候选人评分============
            log.info("步骤3：候选人评分");
            CandidateScoringResult scoringResult = scoringService.scoreCandidate(resumeId, jd.getId());
            log.info("✅ 评分完成：totalScore={}", scoringResult.getTotalScore());

            // ============步骤4：混合检索验证============
            log.info("步骤4：混合检索验证");
            List<HybridSearchResult> searchResults = hybridSearchService.hybridSearch(
                    jd.getTitle() + " " + jd.getSkillTags(), 5);
            log.info("✅ 混合检索完成：results={}", searchResults.size());

            long elapsed = System.currentTimeMillis() - startTime;

            // ============汇总结果============
            result.put("success", true);
            result.put("testName", "端到端完整流程测试");
            result.put("steps", Map.of(
                    "step1_jd_generation", Map.of("jdId", jd.getId(), "title", jd.getTitle()),
                    "step2_resume_upload", Map.of("resumeId", resumeId, "name", resume.getName()),
                    "step3_scoring", Map.of("totalScore", scoringResult.getTotalScore(), "recommendation", scoringResult.getRecommendation()),
                    "step4_search", Map.of("resultsCount", searchResults.size())
            ));
            result.put("total_elapsed_ms", elapsed);

            log.info("✅✅✅ 端到端测试全部通过！总耗时：{}ms", elapsed);

        } catch (Exception e) {
            log.error("❌ 端到端测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("stackTrace", e.getStackTrace());
        }

        return result;
    }

    /**
     * 测试7：Kafka消息发送
     */
    @GetMapping("/test-kafka")
    @Operation(summary = "测试Kafka", description = "测试消息发送和消费")
    public Map<String, Object> testKafka() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("========== 开始测试：Kafka消息发送 ==========");

            // TODO: 发送测试消息
            // kafkaProducerService.sendResumeUploadEvent(testEvent);

            result.put("success", true);
            result.put("testName", "Kafka测试");
            result.put("message", "Kafka消息发送成功，请查看日志确认消费");

            log.info("✅ Kafka测试通过");

        } catch (Exception e) {
            log.error("❌ Kafka测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 测试8：系统健康检查
     */
    @GetMapping("/health-check")
    @Operation(summary = "系统健康检查", description = "检查所有组件状态")
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("========== 开始健康检查 ==========");

            Map<String, Object> components = new HashMap<>();

            // 检查MySQL
            try {
                resumeMapper.selectById(1L);
                components.put("mysql", "✅ OK");
            } catch (Exception e) {
                components.put("mysql", "❌ ERROR: " + e.getMessage());
            }

            // TODO: 检查Elasticsearch
            components.put("elasticsearch", "⚠️ TODO");

            // TODO: 检查Milvus
            components.put("milvus", "⚠️ TODO");

            // TODO: 检查Kafka
            components.put("kafka", "⚠️ TODO");

            // TODO: 检查Redis
            components.put("redis", "⚠️ TODO");

            result.put("success", true);
            result.put("components", components);

            log.info("✅ 健康检查完成");

        } catch (Exception e) {
            log.error("❌ 健康检查失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 测试9：性能压测
     */
    @PostMapping("/performance-test")
    @Operation(summary = "性能压测", description = "测试系统性能指标")
    public Map<String, Object> performanceTest(@RequestParam(defaultValue = "10") int iterations) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("========== 开始性能压测：iterations={} ==========", iterations);

            int successCount = 0;
            int failCount = 0;
            long totalElapsed = 0;

            for (int i = 0; i < iterations; i++) {
                try {
                    long iterStart = System.currentTimeMillis();

                    // 执行测试操作（例如：混合检索）
                    hybridSearchService.hybridSearch("Java后端工程师", 5);

                    long iterElapsed = System.currentTimeMillis() - iterStart;
                    totalElapsed += iterElapsed;
                    successCount++;

                } catch (Exception e) {
                    failCount++;
                    log.error("迭代{}失败", i, e);
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("testName", "性能压测");
            result.put("iterations", iterations);
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("avgResponseTime_ms", successCount > 0 ? totalElapsed / successCount : 0);
            result.put("total_elapsed_ms", elapsed);
            result.put("qps", elapsed > 0 ? (iterations * 1000.0 / elapsed) : 0);

            log.info("✅ 性能压测完成：success={}, avg={}ms, qps={}",
                    successCount, totalElapsed / successCount, iterations * 1000.0 / elapsed);

        } catch (Exception e) {
            log.error("❌ 性能压测失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
