package xCloud.controller.recruitment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xCloud.dto.recruitment.JDFeedbackRequest;
import xCloud.dto.recruitment.JDGenerationRequest;
import xCloud.dto.recruitment.JDVO;
import xCloud.entity.Result;
import xCloud.entity.recruitment.JDFeedback;
import xCloud.entity.recruitment.JobDescription;
import xCloud.mapper.recruitment.JDFeedbackMapper;
import xCloud.mapper.recruitment.JobDescriptionMapper;
import xCloud.service.recruitment.JDGenerationService;

import java.util.List;

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

    @Resource
    private JDGenerationService jdGenerationService;

    @Resource
    private JobDescriptionMapper jobDescriptionMapper;

    @Resource
    private JDFeedbackMapper jdFeedbackMapper;

    /**
     * 生成JD（第一版）
     */
    @PostMapping("/generate")
    @Operation(summary = "生成JD", description = "根据HR需求自动生成岗位描述")
    public Result<JDVO> generateJD(@Validated @RequestBody JDGenerationRequest request) {
        long startTime = System.currentTimeMillis();

        JobDescription jd = jdGenerationService.generateJD(request);

        long elapsed = System.currentTimeMillis() - startTime;
        JDVO vo = JDVO.from(jd);
        vo.setElapsedMs(elapsed);
        vo.setMessage("JD生成成功");

        log.info("✅ JD生成成功：id={}, version={}, elapsed={}ms", jd.getId(), jd.getVersion(), elapsed);
        return Result.success(vo);
    }

    /**
     * HR反馈并优化JD
     */
    @PostMapping("/optimize")
    @Operation(summary = "优化JD", description = "根据HR反馈生成新版本JD；feedbackType=approve 发布，reject 归档，adjust 生成新版本")
    public Result<JDVO> optimizeJD(@Validated @RequestBody JDFeedbackRequest request) {
        long startTime = System.currentTimeMillis();

        JobDescription jd = jdGenerationService.optimizeJD(request);

        long elapsed = System.currentTimeMillis() - startTime;
        JDVO vo = JDVO.from(jd);
        vo.setElapsedMs(elapsed);

        if ("approve".equals(request.getFeedbackType())) {
            vo.setMessage("JD已通过并发布");
        } else if ("reject".equals(request.getFeedbackType())) {
            vo.setMessage("JD已拒绝并归档");
        } else {
            vo.setMessage("JD已优化，生成新版本v" + jd.getVersion());
        }

        log.info("✅ JD优化成功：id={}, version={}, feedbackType={}, elapsed={}ms",
                jd.getId(), jd.getVersion(), request.getFeedbackType(), elapsed);
        return Result.success(vo);
    }

    /**
     * 查询JD详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询JD详情", description = "根据ID查询JD完整信息")
    public Result<JDVO> getJD(
            @Parameter(description = "JD ID", required = true, example = "1") @PathVariable Long id) {
        JobDescription jd = jobDescriptionMapper.selectById(id);
        if (jd == null) {
            return Result.error("JD不存在");
        }
        return Result.success(JDVO.from(jd));
    }

    /**
     * 查询JD的所有反馈记录
     */
    @GetMapping("/{id}/feedback")
    @Operation(summary = "查询JD反馈历史", description = "查询指定JD的所有反馈记录")
    public Result<List<JDFeedback>> getJDFeedback(
            @Parameter(description = "JD ID", required = true, example = "1") @PathVariable Long id) {
        return Result.success(jdFeedbackMapper.selectByJdId(id));
    }

    /**
     * 查询HR创建的所有JD
     */
    @GetMapping("/my-jds")
    @Operation(summary = "查询我的JD列表", description = "查询指定HR创建的所有JD")
    public Result<List<JDVO>> getMyJDs(
            @Parameter(description = "HR用户ID", required = true, example = "1") @RequestParam Long hrUserId) {
        List<JobDescription> jdList = jobDescriptionMapper.selectByCreatorId(hrUserId);
        List<JDVO> voList = jdList.stream().map(JDVO::from).toList();
        return Result.success(voList);
    }
}
