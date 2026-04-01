package xCloud.openAiChatModel.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xCloud.service.hdbscanService.HdbscanService;

import java.util.*;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2026/3/17
 * @ClassName RecruitmentQueryService
 */
@Service
public class RecruitmentQueryService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private HdbscanService hdbscanService;

    @Autowired
    private MilvusClientV2 milvusClient;

    public List<Map<String, Object>> searchJobs(String query) {
        Embedding embedding = embeddingModel.embed(query).content();
        List<Float> vector = Arrays.stream(embedding.vector()).boxed().toList();

        Integer categoryId = hdbscanService.cluster(List.of(vector)).get(0);

        SearchReq searchReq = SearchReq.builder()
            .collectionName("job_collection")
            .annsField("vector")
            .data(List.of(vector))
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
