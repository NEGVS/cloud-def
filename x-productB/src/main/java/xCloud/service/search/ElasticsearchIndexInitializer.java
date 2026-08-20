package xCloud.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.StringReader;

/**
 * Elasticsearch索引初始化服务
 * 应用启动时自动创建索引和映射
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class ElasticsearchIndexInitializer implements CommandLineRunner {

    @Autowired
    private ElasticsearchClient esClient;

    private static final String INDEX_NAME = "candidate_resume";

    @Override
    public void run(String... args) throws Exception {
        try {
            // ============检查索引是否存在============
            ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(INDEX_NAME));
            boolean exists = esClient.indices().exists(existsRequest).value();

            if (exists) {
                log.info("✅ ES索引已存在：{}", INDEX_NAME);
                return;
            }

            // ============创建索引（包含mapping和settings）============
            String mappingJson = buildMappingJson();
            CreateIndexRequest createRequest = CreateIndexRequest.of(c -> c
                    .index(INDEX_NAME)
                    .settings(s -> s
                            .numberOfShards("3")
                            .numberOfReplicas("1")
                            .maxResultWindow(10000)
                            .analysis(a -> a
                                    .analyzer("ik_max_word", an -> an.custom(cu -> cu
                                            .tokenizer("ik_max_word")
                                    ))
                                    .analyzer("ik_smart", an -> an.custom(cu -> cu
                                            .tokenizer("ik_smart")
                                    ))
                            )
                    )
                    .withJson(new StringReader(mappingJson))
            );

            esClient.indices().create(createRequest);
            log.info("✅ ES索引创建成功：{}", INDEX_NAME);

        } catch (Exception e) {
            log.error("❌ ES索引初始化失败", e);
        }
    }

    /**
     * 构建索引Mapping（字段映射）
     * 包含：文本字段（ik分词）+ 向量字段（dense_vector）
     */
    private String buildMappingJson() {
        return """
                {
                  "mappings": {
                    "properties": {
                      "id": { "type": "keyword" },
                      "name": {
                        "type": "text",
                        "analyzer": "ik_max_word",
                        "search_analyzer": "ik_smart"
                      },
                      "content": {
                        "type": "text",
                        "analyzer": "ik_max_word",
                        "search_analyzer": "ik_smart"
                      },
                      "skills": {
                        "type": "text",
                        "analyzer": "ik_max_word",
                        "search_analyzer": "ik_smart"
                      },
                      "experience": { "type": "integer" },
                      "education": { "type": "keyword" },
                      "expectedPosition": {
                        "type": "text",
                        "analyzer": "ik_max_word",
                        "search_analyzer": "ik_smart"
                      },
                      "expectedSalary": { "type": "integer" },
                      "city": { "type": "keyword" },
                      "content_vector": {
                        "type": "dense_vector",
                        "dims": 1024,
                        "index": true,
                        "similarity": "cosine"
                      },
                      "source": { "type": "keyword" },
                      "status": { "type": "keyword" },
                      "uploadTime": { "type": "date" },
                      "updateTime": { "type": "date" },
                      "filePath": { "type": "keyword" }
                    }
                  }
                }
                """;
    }
}
