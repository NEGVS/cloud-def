package xcloud.xproduct.config.elasticsearch;

//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.core.IndexResponse;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 索引数据
 * @Author Andy Fan
 * @Date 2025/3/6 16:13
 * @ClassName ElasticsearchService
 * 3. 创建 Elasticsearch 操作 Service
 * 创建索引、添加文档、搜索数据。
 */
@Slf4j
@Service
public class ElasticsearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public void createIndex(String indexName) throws IOException {
        try {
            elasticsearchClient.indices().create(c -> c.index(indexName));
            System.out.println("创建索引成功" + indexName);
        } catch (Exception e) {
            log.error("\n创建索引失败", e);
        }
    }

    public void addDocument(String indexName, String id) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "测试name");
        data.put("description", "测试数据");
        data.put("price", 100);
        data.put("city", "shanghai");
        IndexResponse response = elasticsearchClient.index(i -> i.index(indexName).id(id).document(data));
        System.out.println("添加文档成功,result:" + response.result() + ", id: " + response.id());
    }


}
