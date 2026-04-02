package xCloud.openAiChatModel.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class RecruitmentQueryService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private WebClient webClient;

    @Autowired
    private MilvusClientV2 milvusClient;

    public List<Map<String, Object>> searchJobs(String query) {
        Embedding embedding = embeddingModel.embed(query).content();
        float[] vectorArray = embedding.vector();
        List<Float> vector = new ArrayList<>();
        for (float v : vectorArray) {
            vector.add(v);
        }

        Integer categoryId = webClient.post()
            .uri("http://localhost:8001/predict")
            .bodyValue(Map.of("vectors", List.of(vector)))
            .retrieve()
            .bodyToMono(Map.class)
            .map(resp -> ((List<Integer>) resp.get("cluster_ids")).get(0))
            .block();

        SearchReq searchReq = SearchReq.builder()
            .collectionName("job_collection")
            .annsField("vector")
            .data(List.of(new FloatVec(vector)))
            .filter("category_id == " + categoryId)
            .topK(5)
            .outputFields(Arrays.asList("job_title", "job_desc"))
            .build();

        SearchResp resp = milvusClient.search(searchReq);
        List<Map<String, Object>> results = new ArrayList<>();

        resp.getSearchResults().forEach(result -> result.forEach(item -> {
            Map<String, Object> job = new HashMap<>();
            job.put("title", item.getEntity().get("job_title"));
            job.put("desc", item.getEntity().get("job_desc"));
            job.put("score", item.getScore());
            results.add(job);
        }));

        return results;
    }
}
