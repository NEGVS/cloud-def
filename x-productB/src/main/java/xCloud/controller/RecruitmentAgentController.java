package xCloud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import xCloud.entity.Result;
import xCloud.service.recruitment.ConversationMemoryService;
import xCloud.service.recruitment.PdfChunkService;
import xCloud.service.recruitment.RecruitmentAgentService;

import java.util.concurrent.CompletableFuture;

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
@Tag(name = "智能招聘 Agent", description = "基于 RAG + ReAct 的智能招聘对话接口，支持 PDF 知识库上传、流式对话及会话管理")
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
     * 异步上传 PDF（立即返回任务 ID，后台处理）
     * 适合大文件 / 高并发场景，避免请求超时
     *
     * @param file    PDF 文件
     * @param docType 文档类型：company_doc / job_info
     */
    @Operation(summary = "异步上传 PDF", description = "立即返回，后台处理解析与向量化，适合大文件或高并发场景，避免请求超时")
    @PostMapping(value = "/upload/async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Object> uploadPdfAsync(
            @Parameter(description = "待上传的 PDF 文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "文档类型：company_doc（公司文档）/ job_info（岗位信息）", example = "company_doc") @RequestParam(value = "docType", defaultValue = "company_doc") String docType) {
        try {
            CompletableFuture<int[]> future = pdfChunkService.uploadPdfAsync(file, docType);
            // 注册回调，任务完成后打印日志（不阻塞响应）
            future.thenAccept(r -> log.info("异步上传完成，入库 {} 块，跳过 {} 块", r[0], r[1]))
                  .exceptionally(e -> { log.error("异步上传失败: {}", e.getMessage()); return null; });
            return Result.success("已提交后台处理，文件: " + file.getOriginalFilename());
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("异步上传提交失败: {}", e.getMessage(), e);
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    /**
     * 同步上传 PDF（阻塞直到完成，适合小文件）
     *
     * @param file    PDF 文件
     * @param docType 文档类型：company_doc / job_info
     */
    @Operation(summary = "同步上传 PDF", description = "阻塞等待解析与向量化完成后返回结果，适合小文件；返回入库块数及跳过（Embedding 失败）块数")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Object> uploadPdf(
            @Parameter(description = "待上传的 PDF 文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "文档类型：company_doc（公司文档）/ job_info（岗位信息）", example = "company_doc") @RequestParam(value = "docType", defaultValue = "company_doc") String docType) {
        try {
            int[] result = pdfChunkService.uploadPdf(file, docType);
            return Result.success("上传成功，入库 " + result[0] + " 块，跳过(Embedding失败) " + result[1] + " 块");
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
    @Operation(summary = "Agent 流式对话（SSE）", description = "基于 ReAct 框架进行多轮对话，以 Server-Sent Events 方式流式返回。" +
            "输出标签：[PLAN] 执行计划 | [STEP N] 推理步骤 | [ANSWER] 最终答案 | [ERROR] 异常信息。" +
            "同一 sessionId 共享对话上下文记忆。")
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @Parameter(description = "会话 ID，相同 ID 共享多轮对话记忆", required = true, example = "user_001") @RequestParam String sessionId,
            @Parameter(description = "用户问题", required = true, example = "我们公司的年假政策是什么？") @RequestParam String query) {
        log.info("Agent chat - session={}, query={}", sessionId, query);
        return agentService.chat(sessionId, query);
    }

    /**
     * 清除会话记忆
     */
    @Operation(summary = "清除会话记忆", description = "根据 sessionId 清除该会话的全部对话上下文，下次对话将重新开始")
    @DeleteMapping("/session/{sessionId}")
    public Result<Object> clearSession(
            @Parameter(description = "要清除的会话 ID", required = true, example = "user_001") @PathVariable String sessionId) {
        memoryService.clear(sessionId);
        return Result.success("会话 [" + sessionId + "] 已清除");
    }

    /**
     * 初始化 Milvus Collection（首次部署时调用一次）
     */
    @Operation(summary = "初始化 Milvus Collection", description = "首次部署时调用，创建向量数据库 Collection 及索引；Collection 已存在则幂等跳过，生产环境仅需执行一次")
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
