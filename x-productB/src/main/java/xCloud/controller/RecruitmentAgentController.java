package xCloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import xCloud.entity.Result;
import xCloud.service.recruitment.ConversationMemoryService;
import xCloud.service.recruitment.PdfChunkService;
import xCloud.service.recruitment.RecruitmentAgentService;

/**
 * 智能招聘 Agent HTTP 接口
 *
 * POST /recruitment/upload          上传 PDF（公司文档 / 岗位信息）
 * POST /recruitment/chat            Agent 对话（SSE 流式输出）
 * DELETE /recruitment/session/{id}  清除会话记忆
 * POST /recruitment/collection/init 初始化 Milvus Collection
 */
@Slf4j
@RestController
@RequestMapping("/recruitment")
public class RecruitmentAgentController {

    private final PdfChunkService pdfChunkService;
    private final RecruitmentAgentService agentService;
    private final ConversationMemoryService memoryService;

    public RecruitmentAgentController(PdfChunkService pdfChunkService,
                                       RecruitmentAgentService agentService,
                                       ConversationMemoryService memoryService) {
        this.pdfChunkService = pdfChunkService;
        this.agentService = agentService;
        this.memoryService = memoryService;
    }

    /**
     * 上传 PDF 并向量化入库
     *
     * @param file    PDF 文件
     * @param docType 文档类型：company_doc（公司内部文档）/ job_info（岗位信息）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Object> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "docType", defaultValue = "company_doc") String docType) {
        try {
            if (file.isEmpty()) return Result.error("文件不能为空");
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                return Result.error("仅支持 PDF 文件");
            }
            int count = pdfChunkService.uploadPdf(file, docType);
            return Result.success("上传成功，共切分 " + count + " 个文档块");
        } catch (Exception e) {
            log.error("PDF 上传失败: {}", e.getMessage(), e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * Agent 对话（Server-Sent Events 流式输出）
     *
     * 输出格式：
     *   [PLAN]     → 执行计划
     *   [STEP N]   → ReAct 推理步骤
     *   [ANSWER]   → 最终答案（流式）
     *   [ERROR]    → 异常信息
     *
     * @param sessionId 会话 ID（同一 sessionId 共享对话记忆）
     * @param query     用户问题
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestParam String sessionId,
            @RequestParam String query) {
        log.info("Agent chat - session={}, query={}", sessionId, query);
        return agentService.chat(sessionId, query);
    }

    /**
     * 清除会话记忆
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Object> clearSession(@PathVariable String sessionId) {
        memoryService.clear(sessionId);
        return Result.success("会话 [" + sessionId + "] 已清除");
    }

    /**
     * 初始化 Milvus Collection（首次部署时调用一次）
     */
    @PostMapping("/collection/init")
    public Result<Object> initCollection() {
        try {
            pdfChunkService.ensureCollectionExists();
            return Result.success("Collection 初始化完成");
        } catch (Exception e) {
            log.error("Collection 初始化失败: {}", e.getMessage(), e);
            return Result.error("初始化失败: " + e.getMessage());
        }
    }
}
