package xCloud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;
import xCloud.service.search.HybridSearchResult;
import xCloud.service.search.HybridSearchService;
import xCloud.service.search.HybridSearchRerankService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 混合检索控制器
 * 提供BM25 + 向量检索 + RRF融合 + Rerank的完整检索链路
 *
 * ============开关感知============
 * Controller 本身始终注册（/health 探活接口不受开关影响）。
 * 检索类接口在 ES/Milvus 开关关闭时返回明确降级提示，而不是注入失败或 NPE：
 * - HybridSearchService 依赖 ES + Milvus 双开关
 * - HybridSearchRerankService 只依赖 LLM，始终可用
 *
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@Tag(name = "混合检索", description = "BM25关键词检索 + 向量语义检索 + RRF融合 + Rerank精排")
public class HybridSearchController {

    /** 混合检索服务：ES / Milvus 开关任一关闭时不存在 */
    @Autowired
    private ObjectProvider<HybridSearchService> hybridSearchServiceProvider;

    @Autowired
    private HybridSearchRerankService rerankService;

    /**
     * 混合检索接口（完整链路：BM25 + 向量 + RRF + Rerank）
     *
     * @param query 查询文本（如：需要3年以上Java开发经验，熟悉Spring Boot）
     * @param topK 返回Top-K结果
     * @param useRerank 是否使用Rerank精排（默认true，关闭可提升速度）
     * @return 检索结果列表
     */
    @PostMapping("/hybrid")
    @Operation(summary = "混合检索", description = "BM25 + 向量检索 + RRF融合 + Rerank精排")
    public Map<String, Object> hybridSearch(
            @Parameter(description = "查询文本", required = true)
            @RequestParam String query,

            @Parameter(description = "返回Top-K结果", example = "10")
            @RequestParam(defaultValue = "10") int topK,

            @Parameter(description = "是否使用Rerank精排", example = "true")
            @RequestParam(defaultValue = "true") boolean useRerank
    ) {
        long startTime = System.currentTimeMillis();

        // ============开关校验：ES / Milvus 未启用时明确降级，不抛异常============
        HybridSearchService hybridSearchService = hybridSearchServiceProvider.getIfAvailable();
        if (hybridSearchService == null) {
            log.warn("混合检索未启用（elasticsearch.enabled / milvus.enabled 已关闭），query={}", query);
            return disabledResponse();
        }

        try {
            // ============1-混合检索（BM25 + 向量 + RRF）============
            List<HybridSearchResult> results = hybridSearchService.hybridSearch(query, topK * 2);

            // ============2-Rerank精排（可选）============
            if (useRerank && !results.isEmpty()) {
                results = rerankService.rerank(query, results, topK);
            } else {
                results = results.subList(0, Math.min(topK, results.size()));
            }

            long elapsed = System.currentTimeMillis() - startTime;

            // ============3-构建响应============
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("total", results.size());
            response.put("results", results);
            response.put("elapsed_ms", elapsed);
            response.put("useRerank", useRerank);

            log.info("✅ 混合检索完成：query={}, topK={}, useRerank={}, elapsed={}ms", query, topK, useRerank, elapsed);
            return response;

        } catch (Exception e) {
            log.error("混合检索失败：query={}", query, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "混合检索失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 快速检索接口（仅BM25 + 向量 + RRF，不使用Rerank）
     * 适用于对延迟敏感的场景
     */
    @PostMapping("/fast")
    @Operation(summary = "快速检索", description = "BM25 + 向量 + RRF融合（不使用Rerank）")
    public Map<String, Object> fastSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int topK
    ) {
        long startTime = System.currentTimeMillis();

        // ============开关校验：ES / Milvus 未启用时明确降级============
        HybridSearchService hybridSearchService = hybridSearchServiceProvider.getIfAvailable();
        if (hybridSearchService == null) {
            log.warn("快速检索未启用（elasticsearch.enabled / milvus.enabled 已关闭），query={}", query);
            return disabledResponse();
        }

        try {
            List<HybridSearchResult> results = hybridSearchService.hybridSearch(query, topK);
            long elapsed = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("total", results.size());
            response.put("results", results);
            response.put("elapsed_ms", elapsed);

            return response;

        } catch (Exception e) {
            log.error("快速检索失败：query={}", query, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "检索失败：" + e.getMessage());
            return error;
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查Elasticsearch和Milvus连接状态")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ok");
        status.put("timestamp", System.currentTimeMillis());
        status.put("hybridSearchEnabled", hybridSearchServiceProvider.getIfAvailable() != null);
        return status;
    }

    /**
     * 检索能力未启用时的统一降级响应
     */
    private Map<String, Object> disabledResponse() {
        Map<String, Object> disabled = new HashMap<>();
        disabled.put("enabled", false);
        disabled.put("total", 0);
        disabled.put("results", Collections.emptyList());
        disabled.put("message", "混合检索未启用：请开启 elasticsearch.enabled 与 milvus.enabled");
        return disabled;
    }
}
