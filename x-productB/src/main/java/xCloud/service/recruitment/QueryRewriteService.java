package xCloud.service.recruitment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import xCloud.entity.recruitment.DocumentChunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query Rewriting + HyDE（假设文档嵌入）
 *
 * 解决原始用户问题与文档向量分布不匹配的问题：
 *
 * 1. Query Rewriting：用 LLM 将用户问题改写为 3 个不同角度的子查询，
 *    分别检索后合并，覆盖更广的语义空间，提升召回率。
 *
 * 2. HyDE（Hypothetical Document Embeddings）：让 LLM 先生成一个"假设答案"，
 *    用假设答案的向量去检索，比问题向量更接近文档分布，提升精准度。
 *
 * 使用场景：在 HybridRagService.hybridSearch() 之前调用，
 *           将原始 query 扩展为多个子查询，分别检索后合并去重。
 */
@Slf4j
@Service
public class QueryRewriteService {

    @Value("${ali.baseUrl}")
    private String baseUrl;

    @Value("${ali.api-key}")
    private String apiKey;

    @Value("${ali.chat_model_name}")
    private String model;

    private final WebClient webClient;

    public QueryRewriteService(WebClient webClient) {
        this.webClient = webClient;
    }

    // ─────────────────────────────────────────────
    // 1. Query Rewriting：多角度改写
    // ─────────────────────────────────────────────

    /**
     * 将用户问题改写为 3 个不同角度的子查询。
     *
     * 例：用户问"请假要找谁审批"
     * 改写为：
     *   - "请假审批流程"
     *   - "假期申请审批人是谁"
     *   - "HR 审批请假的步骤"
     *
     * @param query 原始用户问题
     * @return 改写后的子查询列表（含原始 query，共 3-4 条）
     */
    public List<String> rewrite(String query) {
        String prompt = "你是一个搜索查询优化专家。请将以下用户问题改写为3个不同角度的搜索查询，" +
                "每个查询从不同维度表达同一意图，有助于从文档库中召回更多相关内容。\n\n" +
                "用户问题：" + query + "\n\n" +
                "要求：\n" +
                "1. 每行输出一个查询\n" +
                "2. 不要加序号或前缀\n" +
                "3. 保持简洁，每条不超过20字\n" +
                "4. 只输出查询本身，不要解释";

        try {
            String response = callLLM(prompt, 200);
            List<String> rewrites = Arrays.stream(response.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && s.length() >= 2)
                    .limit(3)
                    .collect(Collectors.toList());

            // 始终包含原始 query，保证原意不丢失
            if (!rewrites.contains(query)) {
                rewrites.add(0, query);
            }

            log.info("[QueryRewrite] 原始: {} → 改写: {}", query, rewrites);
            return rewrites;

        } catch (Exception e) {
            log.warn("[QueryRewrite] 改写失败，降级使用原始 query: {}", e.getMessage());
            return List.of(query);
        }
    }

    // ─────────────────────────────────────────────
    // 2. HyDE：生成假设答案用于向量检索
    // ─────────────────────────────────────────────

    /**
     * 生成假设文档（HyDE）。
     *
     * 原理：直接用问题向量检索时，问题和答案的向量分布差异较大。
     * 先让 LLM 生成一个"假设答案"，用假设答案的向量去检索，
     * 与文档向量分布更接近，显著提升精准度。
     *
     * @param query 用户问题
     * @return 假设答案文本（用于后续 Embedding 检索）
     */
    public String generateHypotheticalDocument(String query) {
        String prompt = "请根据以下问题，生成一段简短的假设性回答（100字以内）。" +
                "这个回答不需要完全准确，只需要在语义上与可能的答案相近，用于辅助文档检索。\n\n" +
                "问题：" + query;

        try {
            String hypo = callLLM(prompt, 150);
            log.debug("[HyDE] 生成假设文档: {}", hypo);
            return hypo;
        } catch (Exception e) {
            log.warn("[HyDE] 生成失败，降级使用原始 query: {}", e.getMessage());
            return query;
        }
    }

    // ─────────────────────────────────────────────
    // 3. 多路检索结果去重合并
    // ─────────────────────────────────────────────

    /**
     * 对多路检索结果按 id 去重，保留首次出现的文档（分数最高的在前）。
     *
     * @param lists 多路检索结果列表
     * @return 去重后的合并结果
     */
    public List<DocumentChunk> deduplicateAndMerge(List<List<DocumentChunk>> lists) {
        Map<Long, DocumentChunk> seen = new java.util.LinkedHashMap<>();
        for (List<DocumentChunk> list : lists) {
            for (DocumentChunk chunk : list) {
                // putIfAbsent：同一文档只保留第一次出现（分数最高的那路）
                seen.putIfAbsent(chunk.getId(), chunk);
            }
        }
        return new ArrayList<>(seen.values());
    }

    // ─────────────────────────────────────────────
    // 内部：LLM 调用
    // ─────────────────────────────────────────────

    private String callLLM(String prompt, int maxTokens) {
        Map<?, ?> response = webClient.post()
                .uri(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", List.of(Map.of("role", "user", "content", prompt)),
                        "max_tokens", maxTokens,
                        "temperature", 0.3f
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) throw new RuntimeException("LLM 返回为空");
        List<?> choices = (List<?>) response.get("choices");
        Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
        return (String) message.get("content");
    }
}
