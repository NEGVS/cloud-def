package xCloud.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch配置类
 * 用途：混合检索（BM25 + 向量检索）
 *
 * ============总开关：elasticsearch.enabled============
 * matchIfMissing = true：未显式配置时默认启用，生产环境行为不变。
 * 置为 false 时不创建 ElasticsearchClient，同时 @EnableElasticsearchRepositories 也不会生效，
 * CandidateResumeRepository 等 Repository Bean 不会注册，应用启动不产生任何 ES 连接行为。
 * 依赖 ES 的服务（HybridSearchService / CandidateScoringService 等）同步带上相同开关。
 *
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
@EnableElasticsearchRepositories(basePackages = "xCloud.repository")
public class ElasticsearchConfig {

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.username:}")
    private String username;

    @Value("${elasticsearch.password:}")
    private String password;

    @Value("${elasticsearch.scheme:http}")
    private String scheme;

    /**
     * 创建Elasticsearch客户端
     * 支持密码认证（可选）
     * 懒加载：仅在实际使用时初始化，避免启动时ES未就绪导致失败
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // ============1-构建RestClient（HTTP客户端）============
        RestClient restClient;
        if (username != null && !username.isEmpty()) {
            // 有密码认证
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            restClient = RestClient.builder(new HttpHost(host, port, scheme))
                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();
            log.info("✅ Elasticsearch客户端初始化完成（带认证）: {}://{}:{}", scheme, host, port);
        } else {
            // 无密码认证
            restClient = RestClient.builder(new HttpHost(host, port, scheme)).build();
            log.info("✅ Elasticsearch客户端初始化完成（无认证）: {}://{}:{}", scheme, host, port);
        }

        // ============2-创建传输层（JSON序列化）============
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // ============3-创建ElasticsearchClient============
        return new ElasticsearchClient(transport);
    }
}
