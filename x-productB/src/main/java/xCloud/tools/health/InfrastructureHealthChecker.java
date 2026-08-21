package xCloud.tools.health;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.milvus.v2.client.MilvusClientV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2026/8/21 00:56
 * @ClassName InfrastructureHealthChecker
 */


@Slf4j
@Component
@RequiredArgsConstructor
public class InfrastructureHealthChecker {

    private final ElasticsearchClient elasticsearchClient;

    private final AdminClient kafkaAdminClient;

    private final MilvusClientV2 milvusClient;


    public void checkAll() {

        log.info("");
        log.info("========== 基础设施连接检测 ==========");

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\n===============基础设施连接检测=====================");
        stringBuilder.append("\n");

        stringBuilder.append(checkElasticsearch());
        stringBuilder.append("\n");
        stringBuilder.append(checkKafka());
        stringBuilder.append("\n");
        stringBuilder.append(checkMilvus());
        stringBuilder.append("\n");

        stringBuilder.append("====================================\n");

        log.info(stringBuilder.toString());
        log.info("====================================");
        log.info("");
    }

    /**
     * Elasticsearch
     */
    private String checkElasticsearch() {
        try {
            boolean connected = elasticsearchClient.ping().value();

            if (connected) {
                log.info("Elasticsearch：启动成功");
                return "Elasticsearch：启动成功";
            } else {
                log.error("Elasticsearch：连接失败");
                return "Elasticsearch：连接失败";
            }

        } catch (Exception e) {
            log.error("Elasticsearch：启动失败，原因：{}", getRootMessage(e));
            return "Elasticsearch：启动失败";

        }
    }

    /**
     * Kafka
     */
    private String checkKafka() {
        try {
            ListTopicsResult result =
                    kafkaAdminClient.listTopics();

            result.names().get(5, TimeUnit.SECONDS);

            log.info("Kafka：启动成功");

            return "Kafka：启动成功";

        } catch (Exception e) {
            log.error(
                    "Kafka：启动失败，原因：{}",
                    getRootMessage(e)
            );
            return "Kafka：启动失败";

        }
    }

    /**
     * Milvus
     */
    private String checkMilvus() {
        try {
            milvusClient.listCollections();

            log.info("Milvus：启动成功");
            return "Milvus：启动成功";

        } catch (Exception e) {
            log.error(
                    "Milvus：启动失败，原因：{}",
                    getRootMessage(e)
            );
            return "Milvus：启动失败";

        }
    }


    /**
     * 获取最底层异常信息
     */
    private String getRootMessage(Throwable throwable) {

        Throwable root = throwable;

        while (root.getCause() != null) {
            root = root.getCause();
        }

        return root.getMessage();
    }
}
