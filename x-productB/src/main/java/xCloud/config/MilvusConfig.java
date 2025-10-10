package xCloud.config;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/10 10:43
 * @ClassName MilvusConfig
 */

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {

    @Value("${milvus.host}")
    private String host;

    @Value("${milvus.port}")
    private Integer port;

    @Bean
    public MilvusServiceClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .build();
        //连接到 Milvus 服务器：通过 MilvusServiceClient 类连接到 Milvus 服务器。
        return new MilvusServiceClient(connectParam);
    }
}
