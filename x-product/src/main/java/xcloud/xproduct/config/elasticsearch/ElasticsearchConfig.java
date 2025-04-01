package xcloud.xproduct.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

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
@Slf4j
public class ElasticsearchConfig {

//    @Value("${spring.elasticsearch.uris}")
//    private String elasticsearchUris;
//
//    @Value("${spring.elasticsearch.username:}")
//    private String username;
//
//    @Value("${spring.elasticsearch.password:}")
//    private String password;

    //    创建一个自定义的 RestClient 配置类或方法:
//
//你可以在你的 Spring Boot 配置类中创建一个 @Bean 来配置 RestHighLevelClient。在这个 Bean 的创建过程中，你可以自定义 RestClientBuilder 来禁用 SSL 验证。
//    @Bean
//    public RestHighLevelClient restHighLevelClient() throws Exception {
//        HttpHost[] hosts = parseUris(elasticsearchUris);
//        CredentialsProvider credentialsProvider = null;
//        if (!username.isEmpty() && !password.isEmpty()) {
//            credentialsProvider = new org.apache.http.impl.client.BasicCredentialsProvider();
//            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
//        }
//
//        SSLContext sslContext = SSLContextBuilder
//                .create()
//                .loadTrustMaterial(null, (TrustStrategy) (x509Certificates, s) -> true) // 信任所有证书
//                .build();
//
//        final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
//                sslContext,
//                NoopHostnameVerifier.INSTANCE); // 禁用主机名验证
//
//        return new RestHighLevelClient(
//                RestClient.builder(hosts)
//                        .setHttpClientConfigCallback(
//                                httpAsyncClientBuilder -> httpAsyncClientBuilder
//                                        .setSSLContext(sslContext)
//                                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
//                                        .setDefaultCredentialsProvider(credentialsProvider)
//                        )
//        );
//    }
//
//    private HttpHost[] parseUris(String uris) {
//        return java.util.Arrays.stream(uris.split(","))
//                .map(uri -> {
//                    java.net.URI parsedUri = java.net.URI.create(uri);
//                    return new HttpHost(parsedUri.getHost(), parsedUri.getPort(), parsedUri.getScheme());
//                })
//                .toArray(HttpHost[]::new);
//    }

    //    ------
    @Bean
    public RestClient restClient() {
        log.info("\n-------ElasticsearchConfig.restClient()");
        RestClient restClient = RestClient.builder(HttpHost.create("https://127.0.0.1:9200")).build();
        return restClient;
    }

    @Bean
    public ElasticsearchTransport transport(RestClient restClient) {
        log.info("\n-------ElasticsearchConfig.transport()");
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        log.info("\n-------ElasticsearchConfig.elasticsearchClient()");
        return new ElasticsearchClient(transport);
    }
    //    @Bean
    //    public ElasticsearchClient elasticsearchClient() {
    //        // 1. 配置认证（如果启用安全）
    //        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    //        credentialsProvider.setCredentials(
    //            AuthScope.ANY,
    //            new UsernamePasswordCredentials("elastic", "yourpassword")
    //        );
    //
    //        // 2. 创建底层 REST 客户端
    //        RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "https"))
    //            .setHttpClientConfigCallback(httpClientBuilder ->
    //                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
    //            )
    //            .build();
    //
    //        // 3. 使用 Jackson 映射器创建 Transport
    //        ElasticsearchTransport transport = new RestClientTransport(
    //            restClient,
    //            new JacksonJsonpMapper()
    //        );
    //
    //        // 4. 返回 ElasticsearchClient
    //        return new ElasticsearchClient(transport);
    //    }
}
