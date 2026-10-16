package xCloud.controller.recruitment;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xCloud.dto.recruitment.BatchResultVO;
import xCloud.dto.recruitment.ResumeVO;
import xCloud.entity.Result;
import xCloud.entity.recruitment.Resume;
import xCloud.event.ResumeParsedEvent;
import xCloud.mapper.recruitment.ResumeMapper;
import xCloud.service.kafka.KafkaProducerService;
import xCloud.service.recruitment.ResumeParseService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 简历管理控制器
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@RestController
@RequestMapping("/api/recruitment/resume")
@Tag(name = "简历管理", description = "简历上传、解析、查询")
public class ResumeController {

    /** 批量上传并发上限：保护 LLM / 数据库，避免瞬时打满线程池 */
    private static final int BATCH_CONCURRENCY = 8;

    /** 批量上传整体超时（秒），防止个别慢任务拖死接口 */
    private static final long BATCH_TIMEOUT_SECONDS = 300L;

    @Resource
    private ResumeParseService resumeParseService;

    @Resource
    private KafkaProducerService kafkaProducerService;

    @Resource
    private ResumeMapper resumeMapper;

    @Resource
    @Qualifier("taskExecutor")
    private ThreadPoolExecutor taskExecutor;

    /**
     * 上传简历（同步解析 + 异步Kafka流处理）
     */
    @PostMapping("/upload")
    @Operation(summary = "上传简历", description = "上传PDF/Word简历并解析")
    public Result<ResumeVO> uploadResume(@RequestParam("file") MultipartFile file,
                                         @RequestParam(defaultValue = "upload") String source) {
        long startTime = System.currentTimeMillis();

        // ============1-同步解析简历（返回完整实体，无需再查库）============
        Resume resume = resumeParseService.uploadAndParseResume(file, source);

        // ============2-发送Kafka事件（异步后续处理：向量化、索引）============
        kafkaProducerService.sendResumeParsedEvent(buildResumeParsedEvent(resume));

        long elapsed = System.currentTimeMillis() - startTime;
        ResumeVO vo = ResumeVO.from(resume);
        vo.setElapsedMs(elapsed);
        vo.setMessage("简历上传成功，正在向量化和索引...");

        log.info("✅ 简历上传成功：resumeId={}, name={}, elapsed={}ms", resume.getId(), resume.getName(), elapsed);
        return Result.success(vo);
    }

    /**
     * 批量上传简历（并行处理，有界并发）
     */
    @PostMapping("/batch-upload")
    @Operation(summary = "批量上传简历", description = "批量上传多个简历文件，并行解析")
    public Result<BatchResultVO> batchUploadResumes(@RequestParam("files") List<MultipartFile> files,
                                                    @RequestParam(defaultValue = "upload") String source) {
        long startTime = System.currentTimeMillis();

        BatchResultVO result = new BatchResultVO();
        result.setTotal(files == null ? 0 : files.size());
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setFailures(new ArrayList<>());
        if (files == null || files.isEmpty()) {
            result.setElapsedMs(0L);
            return Result.success(result);
        }

        // ============有界信号量 + 线程池并行解析，单个失败不影响整体============
        Semaphore gate = new Semaphore(BATCH_CONCURRENCY);
        List<CompletableFuture<BatchResultVO.FailItem>> futures = new ArrayList<>(files.size());

        for (MultipartFile file : files) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    gate.acquire();
                    try {
                        Resume resume = resumeParseService.uploadAndParseResume(file, source);
                        kafkaProducerService.sendResumeParsedEvent(buildResumeParsedEvent(resume));
                        return null; // 成功
                    } finally {
                        gate.release();
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new BatchResultVO.FailItem(file.getOriginalFilename(), "任务被中断");
                } catch (Exception e) {
                    log.error("简历上传失败：filename={}", file.getOriginalFilename(), e);
                    return new BatchResultVO.FailItem(file.getOriginalFilename(), e.getMessage());
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
                result.getFailures().add(new BatchResultVO.FailItem(null, "批量上传超时或异常：" + e.getMessage()));
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        result.setElapsedMs(elapsed);
        log.info("✅ 批量上传完成：total={}, success={}, fail={}, elapsed={}ms",
                result.getTotal(), result.getSuccessCount(), result.getFailCount(), elapsed);
        return Result.success(result);
    }

    /**
     * 查询简历详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询简历详情", description = "根据ID查询简历完整信息")
    public Result<ResumeVO> getResume(
            @Parameter(description = "简历ID", required = true, example = "1") @PathVariable Long id) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) {
            return Result.error("简历不存在");
        }
        return Result.success(ResumeVO.from(resume));
    }

    /**
     * 查询简历列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "查询简历列表", description = "分页查询简历列表，支持按解析状态过滤")
    public Result<IPage<ResumeVO>> listResumes(
            @Parameter(description = "页码，从1开始", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "解析状态（pending、parsing、success、failed）") @RequestParam(required = false) String parseStatus) {
        Page<ResumeVO> pageParam = new Page<>(page, pageSize);
        IPage<ResumeVO> result = resumeMapper.pageResumeList(pageParam, parseStatus);
        return Result.success(result);
    }

    /**
     * 构建ResumeParsedEvent
     */
    private ResumeParsedEvent buildResumeParsedEvent(Resume resume) {
        ResumeParsedEvent event = new ResumeParsedEvent();
        event.setResumeId(resume.getId());
        event.setName(resume.getName());
        event.setPhone(resume.getPhone());
        event.setEmail(resume.getEmail());
        event.setSkills(resume.getSkills());
        event.setWorkYears(resume.getWorkYears());
        event.setExpectedPosition(resume.getExpectedPosition());
        event.setExpectedSalary(resume.getExpectedSalary());
        event.setRawContent(resume.getRawContent());
        event.setEventTime(new Date());
        return event;
    }
}
