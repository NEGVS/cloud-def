package xCloud.service.recruitment;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import xCloud.entity.recruitment.DocumentChunk;
import xCloud.mapper.DocumentChunkMapper;
import xCloud.openAiChatModel.ali.embedding.AliEmbeddingUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 多路召回（Hybrid RAG）
 *
 * 策略：
 *   - 路1：向量检索（Milvus，语义相似度）
 *   - 路2：关键词检索（MySQL LIKE，精确匹配）
 *   - 融合：RRF（Reciprocal Rank Fusion），公式 score = Σ 1/(k + rank_i)，k=60
 */
@Slf4j
@Service
public class HybridRagService {

    private static final int RRF_K = 60;

    @Lazy
    @Resource
    private MilvusClientV2 milvusClientV2;

    @Resource
    private AliEmbeddingUtil aliEmbeddingUtil;

    @Resource
    private DocumentChunkMapper documentChunkMapper;

    // ─────────────────────────────────────────────
    // 主入口：多路召回 + RRF 融合
    // ─────────────────────────────────────────────

    /**
     * @param query 用户查询
     * @param topK  最终返回数量
     */
    public List<DocumentChunk> hybridSearch(String query, int topK) {
        int candidateK = topK * 3; // 每路多召回，融合后再截断

        List<DocumentChunk> vectorResults  = vectorSearch(query, candidateK);
        List<DocumentChunk> keywordResults = keywordSearch(query, candidateK);

        return rrfFusion(vectorResults, keywordResults, topK);
    }

    // ─────────────────────────────────────────────
    // 路1：向量检索
    // ─────────────────────────────────────────────

    public List<DocumentChunk> vectorSearch(String query, int topK) {
        List<Double> vec = aliEmbeddingUtil.embeddingB(query);
        if (vec == null) return Collections.emptyList();

        float[] arr = new float[vec.size()];
        for (int i = 0; i < vec.size(); i++) arr[i] = vec.get(i).floatValue();

        SearchResp resp = milvusClientV2.search(SearchReq.builder()
                .collectionName(PdfChunkService.COLLECTION)
                .data(List.of(new FloatVec(arr)))
                .topK(topK)
                .outputFields(Arrays.asList("id", "content", "source", "doc_type", "chunk_index", "page_num"))
                .build());

        List<DocumentChunk> results = new ArrayList<>();
        for (List<SearchResp.SearchResult> group : resp.getSearchResults()) {
            for (SearchResp.SearchResult item : group) {
                DocumentChunk chunk = mapSearchResult(item);
                results.add(chunk);
            }
        }
        log.debug("向量检索返回 {} 条", results.size());
        return results;
    }

    // ─────────────────────────────────────────────
    // 路2：关键词检索
    // ─────────────────────────────────────────────

    public List<DocumentChunk> keywordSearch(String query, int topK) {
        // 简单分词：按空格、标点切分，过滤停用词
        List<String> keywords = tokenize(query);
        if (keywords.isEmpty()) return Collections.emptyList();

        List<DocumentChunk> results = documentChunkMapper.keywordSearch(keywords, topK);
        log.debug("关键词检索返回 {} 条", results.size());
        return results;
    }

    // ─────────────────────────────────────────────
    // RRF 融合
    // ─────────────────────────────────────────────

    private List<DocumentChunk> rrfFusion(
            List<DocumentChunk> list1,
            List<DocumentChunk> list2,
            int topK) {

        Map<Long, Float> rrfScores = new LinkedHashMap<>();
        Map<Long, DocumentChunk> chunkMap = new LinkedHashMap<>();

        addRrfScores(list1, rrfScores, chunkMap);
        addRrfScores(list2, rrfScores, chunkMap);

        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Float>comparingByValue().reversed())
                .limit(topK)
                .map(e -> {
                    DocumentChunk c = chunkMap.get(e.getKey());
                    c.setScore(e.getValue());
                    return c;
                })
                .collect(Collectors.toList());
    }

    private void addRrfScores(List<DocumentChunk> list,
                               Map<Long, Float> scores,
                               Map<Long, DocumentChunk> chunkMap) {
        for (int rank = 0; rank < list.size(); rank++) {
            DocumentChunk chunk = list.get(rank);
            float rrfScore = 1.0f / (RRF_K + rank + 1);
            scores.merge(chunk.getId(), rrfScore, (a, b) -> a + b);
            chunkMap.putIfAbsent(chunk.getId(), chunk);
        }
    }

    // ─────────────────────────────────────────────
    // 工具方法
    // ─────────────────────────────────────────────

    private List<String> tokenize(String query) {
        // 按非中文非字母数字切分，过滤长度 < 2 的词
        String[] tokens = query.split("[\\s\\p{Punct}，。！？、；：【】（）]+");
        return Arrays.stream(tokens)
                .map(String::trim)
                .filter(t -> t.length() >= 2)
                .distinct()
                .collect(Collectors.toList());
    }

    private DocumentChunk mapSearchResult(SearchResp.SearchResult item) {
        DocumentChunk chunk = new DocumentChunk();
        Map<String, Object> entity = item.getEntity();
        chunk.setId(toLong(entity.get("id")));
        chunk.setContent(str(entity.get("content")));
        chunk.setSource(str(entity.get("source")));
        chunk.setDocType(str(entity.get("doc_type")));
        chunk.setChunkIndex(toInt(entity.get("chunk_index")));
        chunk.setPageNum(toInt(entity.get("page_num")));
        chunk.setScore(item.getScore());
        return chunk;
    }

    private Long toLong(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private Integer toInt(Object o) {
        return o == null ? 0 : ((Number) o).intValue();
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
