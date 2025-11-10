package xCloud.config;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/10 10:43
 * @ClassName MilvusConfig
 */

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {

    @Value("${milvus.host}")
    private String host;

    @Value("${milvus.port}")
    private Integer port;

    @Value("${milvus.uri}")
    private String milvusUri;

    @Value("${milvus.token}")
    private String token;

//    @Bean
//    public MilvusServiceClient milvusClient() {
//        ConnectParam connectParam = ConnectParam.newBuilder()
//                .withHost(host)
//                .withPort(port)
//                .build();
//        //连接到 Milvus 服务器：通过 MilvusServiceClient 类连接到 Milvus 服务器。
//        return new MilvusServiceClient(connectParam);
//    }

//    --------v2----------
    @Bean
    public MilvusClientV2 milvusClientV2() {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(milvusUri)
                .token(token)  // 或使用 .username("root").password("Milvus")
//                .password("Milvus")
//                .username("root")
                .build();
        return new MilvusClientV2(connectConfig);
    }
}
