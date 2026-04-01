package com.recruit.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class VectorSearchService {

    @Autowired
    private MilvusClientV2 milvusClient;

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * 向量检索职位
     */
    public List<Map<String, Object>> searchJobs(String query, Integer categoryId) {
        List<Float> vector = embeddingService.generateEmbedding(query);

        SearchReq searchReq = SearchReq.builder()
            .collectionName("job_collection")
            .data(Collections.singletonList(vector))
            .filter("category_id == " + categoryId)
            .topK(5)
            .outputFields(Arrays.asList("job_title", "job_desc", "category_id"))
            .build();

        SearchResp resp = milvusClient.search(searchReq);

        List<Map<String, Object>> results = new ArrayList<>();
        resp.getSearchResults().forEach(result -> {
            result.forEach(item -> {
                Map<String, Object> job = new HashMap<>();
                job.put("title", item.getEntity().get("job_title"));
                job.put("desc", item.getEntity().get("job_desc"));
                job.put("score", item.getDistance());
                results.add(job);
            });
        });
        return results;
    }
}
