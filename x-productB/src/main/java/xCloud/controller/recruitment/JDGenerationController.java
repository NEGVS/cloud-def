package xCloud.controller.recruitment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xCloud.dto.recruitment.JDFeedbackRequest;
import xCloud.dto.recruitment.JDGenerationRequest;
import xCloud.entity.recruitment.JDFeedback;
import xCloud.entity.recruitment.JobDescription;
import xCloud.mapper.recruitment.JDFeedbackMapper;
import xCloud.mapper.recruitment.JobDescriptionMapper;
import xCloud.service.recruitment.JDGenerationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JD生成与优化控制器
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@RestController
@RequestMapping("/api/recruitment/jd")
@Tag(name = "JD生成与优化", description = "AI生成岗位描述并支持HR反馈迭代优化")
public class JDGenerationController {

    @Autowired
    private JDGenerationService jdGenerationService;

    @Autowired
    private JobDescriptionMapper jobDescriptionMapper;

    @Autowired
    private JDFeedbackMapper jdFeedbackMapper;

    /**
     * 生成JD（第一版）
     */
    @PostMapping("/generate")
    @Operation(summary = "生成JD", description = "根据HR需求自动生成岗位描述")
    public Map<String, Object> generateJD(@Validated @RequestBody JDGenerationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            JobDescription jd = jdGenerationService.generateJD(request);
            long elapsed = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jd", jd);
            response.put("elapsed_ms", elapsed);
            response.put("message", "JD生成成功");

            log.info("✅ JD生成成功：id={}, version={}, elapsed={}ms", jd.getId(), jd.getVersion(), elapsed);
            return response;

        } catch (Exception e) {
            log.error("❌ JD生成失败：title={}", request.getTitle(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "JD生成失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * HR反馈并优化JD
     */
    @PostMapping("/optimize")
    @Operation(summary = "优化JD", description = "根据HR反馈生成新版本JD")
    public Map<String, Object> optimizeJD(@Validated @RequestBody JDFeedbackRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            JobDescription jd = jdGenerationService.optimizeJD(request);
            long elapsed = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jd", jd);
            response.put("elapsed_ms", elapsed);

            if ("approve".equals(request.getFeedbackType())) {
                response.put("message", "JD已通过并发布");
            } else if ("reject".equals(request.getFeedbackType())) {
                response.put("message", "JD已拒绝并归档");
            } else {
                response.put("message", "JD已优化，生成新版本v" + jd.getVersion());
            }

            log.info("✅ JD优化成功：id={}, version={}, feedbackType={}, elapsed={}ms",
                    jd.getId(), jd.getVersion(), request.getFeedbackType(), elapsed);
            return response;

        } catch (Exception e) {
            log.error("❌ JD优化失败：jdId={}", request.getJdId(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "JD优化失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 查询JD详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询JD详情", description = "根据ID查询JD完整信息")
    public Map<String, Object> getJD(@PathVariable Long id) {
        try {
            JobDescription jd = jobDescriptionMapper.selectById(id);
            if (jd == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "JD不存在");
                return error;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jd", jd);
            return response;

        } catch (Exception e) {
            log.error("❌ 查询JD失败：id={}", id, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 查询JD的所有反馈记录
     */
    @GetMapping("/{id}/feedback")
    @Operation(summary = "查询JD反馈历史", description = "查询指定JD的所有反馈记录")
    public Map<String, Object> getJDFeedback(@PathVariable Long id) {
        try {
            List<JDFeedback> feedbackList = jdFeedbackMapper.selectByJdId(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", feedbackList.size());
            response.put("feedbackList", feedbackList);
            return response;

        } catch (Exception e) {
            log.error("❌ 查询JD反馈失败：id={}", id, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 查询HR创建的所有JD
     */
    @GetMapping("/my-jds")
    @Operation(summary = "查询我的JD列表", description = "查询指定HR创建的所有JD")
    public Map<String, Object> getMyJDs(@RequestParam Long hrUserId) {
        try {
            List<JobDescription> jdList = jobDescriptionMapper.selectByCreatorId(hrUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", jdList.size());
            response.put("jdList", jdList);
            return response;

        } catch (Exception e) {
            log.error("❌ 查询JD列表失败：hrUserId={}", hrUserId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败：" + e.getMessage());
            return error;
        }
    }
}
