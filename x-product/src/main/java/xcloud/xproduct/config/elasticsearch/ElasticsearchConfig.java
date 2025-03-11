package xcloud.xproduct.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description 2. 配置 Elasticsearch 连接
 * 创建 ElasticsearchConfig 配置类，使用 ElasticsearchClient 连接 Elasticsearch。
 * @Author Andy Fan
 * @Date 2025/3/6 16:03
 * @ClassName ElasticsearchConfig
 * - RestClient 用于创建低级别 REST 客户端，连接 http://localhost:9200 。
 * - ElasticsearchTransport 作为传输层，使用 JacksonJsonpMapper 解析 JSON。
 * - ElasticsearchClient 作为主客户端，提供操作 Elasticsearch 的 API。
 */
@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestClient restClient() {
        RestClient restClient = RestClient.builder(HttpHost.create("https://127.0.0.1:9200")).build();
        return restClient;
    }

    @Bean
    public ElasticsearchTransport transport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
