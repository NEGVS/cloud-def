package xcloud.xproduct.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/3/6 17:16
 * @ClassName ElasticsearchQueryService
 */
@Service
public class ElasticsearchQueryService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public void searchDocument(String indexName) throws IOException {
        //q.matchAll(m -> m)
        SearchResponse<Map> response = elasticsearchClient.search(s -> s
                .index(indexName)
                .query(q -> q.match(m -> m.field("name").query("andy"))), Map.class);
        List<Hit<Map>> hits = response.hits().hits();
        for (Hit<Map> hit : hits) {
            System.out.println("result: " + hit.source());
        }
    }
}
