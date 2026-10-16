package xCloud.controller.recruitment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import xCloud.dto.recruitment.BatchResultVO;
import xCloud.dto.recruitment.CandidateScoringResult;
import xCloud.entity.Result;
import xCloud.entity.recruitment.CandidateScore;
import xCloud.mapper.recruitment.CandidateScoreMapper;
import xCloud.service.recruitment.CandidateScoringService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 候选人评分控制器
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@RestController
@RequestMapping("/api/recruitment/scoring")
@Tag(name = "候选人评分引擎", description = "多维度候选人评分与推荐")
public class CandidateScoringController {

    /** 批量评分并发上限：每次评分含一次 LLM 调用，必须限流保护 */
    private static final int BATCH_CONCURRENCY = 8;

    /** 批量评分整体超时（秒） */
    private static final long BATCH_TIMEOUT_SECONDS = 600L;

    @Resource
    private CandidateScoringService scoringService;

    @Resource
    private CandidateScoreMapper candidateScoreMapper;

    @Resource
    @Qualifier("taskExecutor")
    private ThreadPoolExecutor taskExecutor;

    /**
     * 对候选人进行评分
     */
    @PostMapping("/score")
    @Operation(summary = "候选人评分", description = "对候选人进行多维度评分并生成推荐")
    public Result<CandidateScoringResult> scoreCandidate(
            @Parameter(description = "候选人ID", required = true, example = "1") @RequestParam Long candidateId,
            @Parameter(description = "JD ID", required = true, example = "1") @RequestParam Long jdId) {
        long startTime = System.currentTimeMillis();

        CandidateScoringResult result = scoringService.scoreCandidate(candidateId, jdId);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("✅ 候选人评分成功：candidateId={}, jdId={}, totalScore={}, elapsed={}ms",
                candidateId, jdId, result.getTotalScore(), elapsed);
        return Result.success(result);
    }

    /**
     * 批量评分（为JD下的多个候选人并行评分）
     */
    @PostMapping("/batch-score")
    @Operation(summary = "批量评分", description = "为指定JD下的多个候选人并行评分")
    public Result<BatchResultVO> batchScore(
            @Parameter(description = "JD ID", required = true, example = "1") @RequestParam Long jdId,
            @RequestBody List<Long> candidateIds) {
        long startTime = System.currentTimeMillis();

        BatchResultVO result = new BatchResultVO();
        result.setTotal(candidateIds == null ? 0 : candidateIds.size());
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setFailures(new ArrayList<>());
        if (candidateIds == null || candidateIds.isEmpty()) {
            result.setElapsedMs(0L);
            return Result.success(result);
        }

        // ============有界信号量 + 线程池并行评分，单个失败不影响整体============
        Semaphore gate = new Semaphore(BATCH_CONCURRENCY);
        List<CompletableFuture<BatchResultVO.FailItem>> futures = new ArrayList<>(candidateIds.size());

        for (Long candidateId : candidateIds) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    gate.acquire();
                    try {
                        scoringService.scoreCandidate(candidateId, jdId);
                        return null; // 成功
                    } finally {
                        gate.release();
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new BatchResultVO.FailItem(String.valueOf(candidateId), "任务被中断");
                } catch (Exception e) {
                    log.error("候选人评分失败：candidateId={}", candidateId, e);
                    return new BatchResultVO.FailItem(String.valueOf(candidateId), e.getMessage());
                }
            }, taskExecutor));
        }

        for (CompletableFuture<BatchResultVO.FailItem> future : futures) {
            try {
                BatchResultVO.FailItem failItem = future.get(BATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (failItem == null) {
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } else {
                    result.setFailCount(result.getFailCount() + 1);
                    result.getFailures().add(failItem);
                }
            } catch (Exception e) {
                result.setFailCount(result.getFailCount() + 1);
                result.getFailures().add(new BatchResultVO.FailItem(null, "批量评分超时或异常：" + e.getMessage()));
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        result.setElapsedMs(elapsed);
        log.info("✅ 批量评分完成：jdId={}, total={}, success={}, fail={}, elapsed={}ms",
                jdId, result.getTotal(), result.getSuccessCount(), result.getFailCount(), elapsed);
        return Result.success(result);
    }

    /**
     * 查询JD下所有候选人的评分（按总分降序）
     */
    @GetMapping("/jd-candidates")
    @Operation(summary = "查询JD候选人排名", description = "查询指定JD下所有候选人评分，按总分降序")
    public Result<List<CandidateScore>> getJDCandidates(
            @Parameter(description = "JD ID", required = true, example = "1") @RequestParam Long jdId) {
        return Result.success(candidateScoreMapper.selectByJdIdOrderByScore(jdId));
    }

    /**
     * 查询候选人的评分详情
     */
    @GetMapping("/candidate-detail")
    @Operation(summary = "查询候选人评分详情", description = "查询候选人在指定JD下的评分详情")
    public Result<CandidateScore> getCandidateScore(
            @Parameter(description = "候选人ID", required = true, example = "1") @RequestParam Long candidateId,
            @Parameter(description = "JD ID", required = true, example = "1") @RequestParam Long jdId) {
        CandidateScore score = candidateScoreMapper.selectByCandidateAndJd(candidateId, jdId);
        if (score == null) {
            return Result.error("未找到评分记录，请先进行评分");
        }
        return Result.success(score);
    }

    /**
     * 查询高分候选人（推荐候选人）
     */
    @GetMapping("/recommended")
    @Operation(summary = "查询推荐候选人", description = "查询指定JD下的高分候选人（默认总分>=85分）")
    public Result<List<CandidateScore>> getRecommendedCandidates(
            @Parameter(description = "JD ID", required = true, example = "1") @RequestParam Long jdId,
            @Parameter(description = "总分阈值", example = "85") @RequestParam(defaultValue = "85") Integer threshold) {
        return Result.success(candidateScoreMapper.selectHighScoreCandidates(jdId, threshold));
    }
}
