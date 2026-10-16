package xCloud.tools.health;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.milvus.v2.client.MilvusClientV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Description 基础设施健康检查
 * @Author Andy Fan
 * @Date 2026/8/21 00:56
 * @ClassName InfrastructureHealthChecker
 *
 * ============开关感知============
 * ES / Kafka / Milvus 三个客户端均通过 ObjectProvider 按需获取：
 * 对应总开关关闭时 Bean 不存在，直接输出"未启用（已通过开关关闭）"，不做任何连接尝试，也不抛异常。
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class InfrastructureHealthChecker {

    /** ES 客户端（elasticsearch.enabled=false 时不存在） */
    private final ObjectProvider<ElasticsearchClient> elasticsearchClientProvider;

    /** Kafka AdminClient（kafka.enabled=false 时不存在） */
    private final ObjectProvider<AdminClient> kafkaAdminClientProvider;

    /** Milvus 客户端（milvus.enabled=false 时不存在） */
    private final ObjectProvider<MilvusClientV2> milvusClientProvider;

    /**
     * 依次检测三件基础设施，输出汇总报告
     */
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
        ElasticsearchClient elasticsearchClient = elasticsearchClientProvider.getIfAvailable();
        if (elasticsearchClient == null) {
            log.info("Elasticsearch：未启用（elasticsearch.enabled=false）");
            return "Elasticsearch：未启用（已通过开关关闭）";
        }

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
        AdminClient kafkaAdminClient = kafkaAdminClientProvider.getIfAvailable();
        if (kafkaAdminClient == null) {
            log.info("Kafka：未启用（kafka.enabled=false）");
            return "Kafka：未启用（已通过开关关闭）";
        }

        try {
            ListTopicsResult result = kafkaAdminClient.listTopics();

            result.names().get(5, TimeUnit.SECONDS);

            log.info("Kafka：启动成功");

            return "Kafka：启动成功";

        } catch (Exception e) {
            log.error("Kafka：启动失败，原因：{}", getRootMessage(e));
            return "Kafka：启动失败";
        }
    }

    /**
     * Milvus
     */
    private String checkMilvus() {
        MilvusClientV2 milvusClient = milvusClientProvider.getIfAvailable();
        if (milvusClient == null) {
            log.info("Milvus：未启用（milvus.enabled=false）");
            return "Milvus：未启用（已通过开关关闭）";
        }

        try {
            milvusClient.listCollections();

            log.info("Milvus：启动成功");
            return "Milvus：启动成功";

        } catch (Exception e) {
            log.error("Milvus：启动失败，原因：{}", getRootMessage(e));
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
