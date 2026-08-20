package xCloud.controller.recruitment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.recruitment.Resume;
import xCloud.event.ResumeParsedEvent;
import xCloud.event.ResumeUploadEvent;
import xCloud.mapper.recruitment.ResumeMapper;
import xCloud.service.kafka.KafkaProducerService;
import xCloud.service.recruitment.ResumeParseService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private ResumeParseService resumeParseService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private ResumeMapper resumeMapper;

    /**
     * 上传简历（同步解析 + 异步Kafka流处理）
     */
    @PostMapping("/upload")
    @Operation(summary = "上传简历", description = "上传PDF/Word简历并解析")
    public Map<String, Object> uploadResume(@RequestParam("file") MultipartFile file,
                                            @RequestParam(defaultValue = "upload") String source) {
        long startTime = System.currentTimeMillis();

        try {
            // ============1-同步解析简历============
            Long resumeId = resumeParseService.uploadAndParseResume(file, source);
            Resume resume = resumeMapper.selectById(resumeId);

            // ============2-发送Kafka事件（异步后续处理）============
            ResumeParsedEvent parsedEvent = buildResumeParsedEvent(resume);
            kafkaProducerService.sendResumeParsedEvent(parsedEvent);

            long elapsed = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resumeId", resumeId);
            response.put("name", resume.getName());
            response.put("phone", resume.getPhone());
            response.put("expectedPosition", resume.getExpectedPosition());
            response.put("elapsed_ms", elapsed);
            response.put("message", "简历上传成功，正在向量化和索引...");

            log.info("✅ 简历上传成功：resumeId={}, name={}, elapsed={}ms", resumeId, resume.getName(), elapsed);
            return response;

        } catch (Exception e) {
            log.error("❌ 简历上传失败：filename={}", file.getOriginalFilename(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "简历上传失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 批量上传简历
     */
    @PostMapping("/batch-upload")
    @Operation(summary = "批量上传简历", description = "批量上传多个简历文件")
    public Map<String, Object> batchUploadResumes(@RequestParam("files") List<MultipartFile> files,
                                                   @RequestParam(defaultValue = "upload") String source) {
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;

        for (MultipartFile file : files) {
            try {
                Long resumeId = resumeParseService.uploadAndParseResume(file, source);
                Resume resume = resumeMapper.selectById(resumeId);

                // 发送Kafka事件
                ResumeParsedEvent parsedEvent = buildResumeParsedEvent(resume);
                kafkaProducerService.sendResumeParsedEvent(parsedEvent);

                successCount++;
            } catch (Exception e) {
                log.error("简历上传失败：filename={}", file.getOriginalFilename(), e);
                failCount++;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("total", files.size());
        response.put("successCount", successCount);
        response.put("failCount", failCount);
        response.put("elapsed_ms", elapsed);

        log.info("✅ 批量上传完成：total={}, success={}, fail={}, elapsed={}ms",
                files.size(), successCount, failCount, elapsed);
        return response;
    }

    /**
     * 查询简历详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询简历详情", description = "根据ID查询简历完整信息")
    public Map<String, Object> getResume(@PathVariable Long id) {
        try {
            Resume resume = resumeMapper.selectById(id);
            if (resume == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "简历不存在");
                return error;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resume", resume);
            return response;

        } catch (Exception e) {
            log.error("❌ 查询简历失败：id={}", id, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 查询简历列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "查询简历列表", description = "分页查询简历列表")
    public Map<String, Object> listResumes(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "20") Integer pageSize,
                                           @RequestParam(required = false) String parseStatus) {
        try {
            // TODO: 实现分页查询
            List<Resume> resumes = resumeMapper.selectByParseStatus(parseStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", resumes.size());
            response.put("resumes", resumes);
            return response;

        } catch (Exception e) {
            log.error("❌ 查询简历列表失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询失败：" + e.getMessage());
            return error;
        }
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
