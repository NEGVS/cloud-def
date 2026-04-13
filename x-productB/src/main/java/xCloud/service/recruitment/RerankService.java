package xCloud.service.recruitment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import xCloud.entity.recruitment.DocumentChunk;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 二次排序（Re-ranking）
 *
 * 使用阿里云 DashScope gte-rerank 模型对召回结果重排序，
 * 比纯向量相似度更准确地衡量 query 与 document 的相关性。
 *
 * API: POST https://dashscope.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank
 */
@Slf4j
@Service
public class RerankService {

    private static final String RERANK_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank";
    private static final String RERANK_MODEL = "gte-rerank";

    @Value("${ali.api-key}")
    private String apiKey;

    private final WebClient webClient;

    public RerankService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 对候选文档重排序
     *
     * @param query  用户查询
     * @param docs   候选文档列表
     * @param topK   返回 top-K
     * @return 重排后的文档列表
     */
    public List<DocumentChunk> rerank(String query, List<DocumentChunk> docs, int topK) {
        if (docs.isEmpty()) return docs;

        List<String> documents = docs.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.toList());

        Map<String, Object> input = new HashMap<>();
        input.put("query", query);
        input.put("documents", documents);

        Map<String, Object> params = new HashMap<>();
        params.put("top_n", Math.min(topK, docs.size()));
        params.put("return_documents", false);

        Map<String, Object> body = new HashMap<>();
        body.put("model", RERANK_MODEL);
        body.put("input", input);
        body.put("parameters", params);

        try {
            Map<?, ?> response = webClient.post()
                    .uri(RERANK_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return docs.subList(0, Math.min(topK, docs.size()));

            List<?> results = (List<?>) ((Map<?, ?>) response.get("output")).get("results");
            List<DocumentChunk> reranked = new ArrayList<>();
            for (Object r : results) {
                Map<?, ?> item = (Map<?, ?>) r;
                int index = ((Number) item.get("index")).intValue();
                float score = ((Number) item.get("relevance_score")).floatValue();
                DocumentChunk chunk = docs.get(index);
                chunk.setScore(score);
                reranked.add(chunk);
            }
            log.debug("Rerank 完成，返回 {} 条", reranked.size());
            return reranked;

        } catch (Exception e) {
            log.warn("Rerank API 调用失败，降级使用原始顺序: {}", e.getMessage());
            return docs.subList(0, Math.min(topK, docs.size()));
        }
    }
}
