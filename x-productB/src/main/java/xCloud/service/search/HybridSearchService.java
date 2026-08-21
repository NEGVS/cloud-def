package xCloud.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xCloud.entity.es.CandidateResumeDocument;
import xCloud.service.vector.EmbeddingService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合检索服务（BM25 + 向量检索 + RRF融合）
 * 核心算法：Reciprocal Rank Fusion (RRF)
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class HybridSearchService {

    @Autowired
    private ElasticsearchClient esClient;

    @Autowired
    private MilvusClientV2 milvusClient;

    @Autowired
    private EmbeddingService embeddingService;

    @Value("${vector.collection:andy_vectorsV2}")
    private String collectionName;

    /**
     * RRF常量k（推荐值60，论文标准值）
     */
    private static final int RRF_K = 60;

    /**
     * 混合检索主入口
     * @param query 用户查询文本
     * @param topK 返回Top-K结果
     * @return 融合后的检索结果
     */
    public List<HybridSearchResult> hybridSearch(String query, int topK) {
        try {
            // ============1-BM25关键词检索============
            List<HybridSearchResult> bm25Results = bm25Search(query, topK * 2);
            log.info("BM25检索完成，召回{}条", bm25Results.size());

            // ============2-向量语义检索============
            List<HybridSearchResult> vectorResults = vectorSearch(query, topK * 2);
            log.info("向量检索完成，召回{}条", vectorResults.size());

            // ============3-RRF融合============
            List<HybridSearchResult> fusedResults = rrfFusion(bm25Results, vectorResults);
            log.info("RRF融合完成，最终结果{}条", fusedResults.size());

            // ============4-返回Top-K============
            return fusedResults.stream().limit(topK).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("混合检索失败：query={}", query, e);
            throw new RuntimeException("混合检索失败", e);
        }
    }

    /**
     * BM25关键词检索（Elasticsearch）
     * 使用ik分词器，multi_match多字段匹配
     */
    private List<HybridSearchResult> bm25Search(String query, int topK) {
        try {
            // ============构建multi_match查询（匹配content、skills、expectedPosition字段）============
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("candidate_resume")
                    .query(q -> q
                            .multiMatch(m -> m
                                    .query(query)
                                    .fields("content^3", "skills^2", "expectedPosition^1.5") // 字段权重
                                    .analyzer("standard")
//                                    standard,ik_smart
                            )
                    )
                    .size(topK)
            );

            SearchResponse<CandidateResumeDocument> response = esClient.search(searchRequest, CandidateResumeDocument.class);

            // ============转换为HybridSearchResult============
            List<HybridSearchResult> results = new ArrayList<>();
            int rank = 1;
            for (Hit<CandidateResumeDocument> hit : response.hits().hits()) {
                CandidateResumeDocument doc = hit.source();
                if (doc == null) continue;

                HybridSearchResult result = new HybridSearchResult();
                result.setDocumentId(doc.getId());
                result.setName(doc.getName());
                result.setContentSnippet(truncate(doc.getContent(), 200));
                result.setSkills(doc.getSkills());
                result.setExperience(doc.getExperience());
                result.setExpectedPosition(doc.getExpectedPosition());
                result.setBm25Score(hit.score());
                result.setRank(rank++);

                results.add(result);
            }

            return results;

        } catch (Exception e) {
            log.error("BM25检索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 向量语义检索（Milvus V2）
     */
    private List<HybridSearchResult> vectorSearch(String query, int topK) {
        try {
            // ============1-查询文本向量化============
            List<Float> queryVector = embeddingService.embed(query);

            // ============2-Milvus V2向量检索============
            SearchReq searchReq = SearchReq.builder()
                    .collectionName(collectionName)
                    .annsField("embedding")
                    .data(Collections.singletonList(new FloatVec(queryVector)))
                    .topK(topK)
                    .outputFields(Arrays.asList("id", "name", "content", "skills", "experience", "expectedPosition"))
                    .build();

            SearchResp searchResp = milvusClient.search(searchReq);

            // ============3-解析结果============
            List<HybridSearchResult> results = new ArrayList<>();
            if (searchResp == null || searchResp.getSearchResults() == null || searchResp.getSearchResults().isEmpty()) {
                log.warn("Milvus返回空结果");
                return results;
            }

            List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
            if (!searchResults.isEmpty()) {
                List<SearchResp.SearchResult> firstQueryResults = searchResults.get(0);
                int rank = 1;

                for (SearchResp.SearchResult item : firstQueryResults) {
                    HybridSearchResult result = new HybridSearchResult();

                    Map<String, Object> entity = item.getEntity();
                    result.setDocumentId(String.valueOf(entity.get("id")));
                    result.setName(String.valueOf(entity.get("name")));
                    result.setContentSnippet(truncate(String.valueOf(entity.get("content")), 200));
                    result.setSkills(String.valueOf(entity.get("skills")));
                    result.setExperience(entity.get("experience") != null ? Integer.parseInt(String.valueOf(entity.get("experience"))) : null);
                    result.setExpectedPosition(String.valueOf(entity.get("expectedPosition")));
                    result.setVectorScore((double) item.getScore());
                    result.setRank(rank++);

                    results.add(result);
                }
            }

            return results;

        } catch (Exception e) {
            log.error("向量检索失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * RRF融合算法（Reciprocal Rank Fusion）
     * 公式：RRF(d) = Σ 1 / (k + rank_i(d))
     *
     * @param bm25Results BM25检索结果
     * @param vectorResults 向量检索结果
     * @return 融合后的结果（按finalScore降序）
     */
    private List<HybridSearchResult> rrfFusion(List<HybridSearchResult> bm25Results, List<HybridSearchResult> vectorResults) {
        // ============1-构建文档ID到结果的映射============
        Map<String, HybridSearchResult> resultMap = new HashMap<>();

        // ============2-计算BM25的RRF分数============
        for (int i = 0; i < bm25Results.size(); i++) {
            HybridSearchResult result = bm25Results.get(i);
            String docId = result.getDocumentId();

            double rrfScore = 1.0 / (RRF_K + i + 1); // rank从1开始，数组索引从0开始，所以+1

            if (resultMap.containsKey(docId)) {
                HybridSearchResult existing = resultMap.get(docId);
                existing.setFinalScore(existing.getFinalScore() + rrfScore);
            } else {
                result.setFinalScore(rrfScore);
                resultMap.put(docId, result);
            }
        }

        // ============3-计算向量检索的RRF分数============
        for (int i = 0; i < vectorResults.size(); i++) {
            HybridSearchResult result = vectorResults.get(i);
            String docId = result.getDocumentId();

            double rrfScore = 1.0 / (RRF_K + i + 1);

            if (resultMap.containsKey(docId)) {
                HybridSearchResult existing = resultMap.get(docId);
                existing.setFinalScore(existing.getFinalScore() + rrfScore);
                existing.setVectorScore(result.getVectorScore()); // 补充向量分数
            } else {
                result.setFinalScore(rrfScore);
                resultMap.put(docId, result);
            }
        }

        // ============4-按finalScore降序排序============
        List<HybridSearchResult> fusedResults = new ArrayList<>(resultMap.values());
        fusedResults.sort((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()));

        // ============5-重新设置排名============
        for (int i = 0; i < fusedResults.size(); i++) {
            fusedResults.get(i).setRank(i + 1);
        }

        return fusedResults;
    }

    /**
     * 截断文本到指定长度
     */
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
