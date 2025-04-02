package xcloud.xproduct.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


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
@PropertySource("classpath:bootstrap.yml")
@Configuration
@Slf4j
public class ElasticsearchConfig {

    //@Value 注解会从 application.properties 或 application.yml 中读取属性,确认文件名是 application.properties 或 application.yml，且位于 src/main/resources 下。
    //如果使用了自定义配置文件（例如 custom.yml），需要通过 @PropertySource 加载：
    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        //设置认证
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        //解析uri
//        HttpHost httpHost = HttpHost.create(elasticsearchUris);
        HttpHost httpHost = new HttpHost("127.0.0.1", 9200, "https");
        //创建RestClient客户端
        RestClient restClient = RestClient.builder(httpHost).setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)).build();
        //创建传输层
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
