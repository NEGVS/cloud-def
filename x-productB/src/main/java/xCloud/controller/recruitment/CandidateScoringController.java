package xCloud.controller.recruitment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xCloud.dto.recruitment.CandidateScoringResult;
import xCloud.entity.recruitment.CandidateScore;
import xCloud.mapper.recruitment.CandidateScoreMapper;
import xCloud.service.recruitment.CandidateScoringService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private CandidateScoringService scoringService;

    @Autowired
    private CandidateScoreMapper candidateScoreMapper;

    /**
     * 对候选人进行评分
     */
    @PostMapping("/score")
    @Operation(summary = "候选人评分", description = "对候选人进行多维度评分并生成推荐")
    public Map<String, Object> scoreCandidate(@RequestParam Long candidateId, @RequestParam Long jdId) {
        long startTime = System.currentTimeMillis();

        try {
            CandidateScoringResult result = scoringService.scoreCandidate(candidateId, jdId);
            long elapsed = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            response.put("elapsed_ms", elapsed);

            log.info("✅ 候选人评分成功：candidateId={}, jdId={}, totalScore={}, elapsed={}ms",
                    candidateId, jdId, result.getTotalScore(), elapsed);
            return response;

        } catch (Exception e) {
            log.error("❌ 候选人评分失败：candidateId={}, jdId={}", candidateId, jdId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "评分失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 批量评分（为JD下的多个候选人评分）
     */
    @PostMapping("/batch-score")
    @Operation(summary = "批量评分", description = "为指定JD下的多个候选人批量评分")
    public Map<String, Object> batchScore(@RequestParam Long jdId, @RequestBody List<Long> candidateIds) {
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;

        try {
            for (Long candidateId : candidateIds) {
                try {
                    scoringService.scoreCandidate(candidateId, jdId);
                    successCount++;
                } catch (Exception e) {
                    log.error("候选人评分失败：candidateId={}", candidateId, e);
                    failCount++;
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", candidateIds.size());
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            response.put("elapsed_ms", elapsed);

            log.info("✅ 批量评分完成：jdId={}, total={}, success={}, fail={}, elapsed={}ms",
                    jdId, candidateIds.size(), successCount, failCount, elapsed);
            return response;

        } catch (Exception e) {
            log.error("❌ 批量评分失败：jdId={}", jdId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "批量评分失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 查询JD下所有候选人的评分（按总分降序）
     */
    @GetMapping("/jd-candidates")
    @Operation(summary = "查询JD候选人排名", description = "查询指定JD下所有候选人评分，按总分降序")
    public Map<String, Object> getJDCandidates(@RequestParam Long jdId) {
        try {
            List<CandidateScore> scores = candidateScoreMapper.selectByJdIdOrderByScore(jdId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jdId", jdId);
            response.put("total", scores.size());
            response.put("candidates", scores);
            return response;

        } catch (Exception e) {
            log.error("❌ 查询JD候选人排名失败：jdId={}", jdId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 查询候选人的评分详情
     */
    @GetMapping("/candidate-detail")
    @Operation(summary = "查询候选人评分详情", description = "查询候选人在指定JD下的评分详情")
    public Map<String, Object> getCandidateScore(@RequestParam Long candidateId, @RequestParam Long jdId) {
        try {
            CandidateScore score = candidateScoreMapper.selectByCandidateAndJd(candidateId, jdId);

            if (score == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "未找到评分记录，请先进行评分");
                return error;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("score", score);
            return response;

        } catch (Exception e) {
            log.error("❌ 查询候选人评分失败：candidateId={}, jdId={}", candidateId, jdId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 查询高分候选人（推荐候选人）
     */
    @GetMapping("/recommended")
    @Operation(summary = "查询推荐候选人", description = "查询指定JD下的高分候选人（总分>=85分）")
    public Map<String, Object> getRecommendedCandidates(@RequestParam Long jdId,
                                                         @RequestParam(defaultValue = "85") Integer threshold) {
        try {
            List<CandidateScore> scores = candidateScoreMapper.selectHighScoreCandidates(jdId, threshold);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jdId", jdId);
            response.put("threshold", threshold);
            response.put("total", scores.size());
            response.put("candidates", scores);
            return response;

        } catch (Exception e) {
            log.error("❌ 查询推荐候选人失败：jdId={}, threshold={}", jdId, threshold, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败：" + e.getMessage());
            return error;
        }
    }
}
