package xCloud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xCloud.service.search.HybridSearchResult;
import xCloud.service.search.HybridSearchService;
import xCloud.service.search.HybridSearchRerankService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 混合检索控制器
 * 提供BM25 + 向量检索 + RRF融合 + Rerank的完整检索链路
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@Tag(name = "混合检索", description = "BM25关键词检索 + 向量语义检索 + RRF融合 + Rerank精排")
public class HybridSearchController {

    @Autowired
    private HybridSearchService hybridSearchService;

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
        // TODO: 添加ES和Milvus的连接状态检查
        return status;
    }
}
