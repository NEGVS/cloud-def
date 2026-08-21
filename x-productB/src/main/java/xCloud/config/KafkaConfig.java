package xCloud.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Kafka配置类
 * 用途：数据中台 - 实时数据流处理
 *
 * @author Claude
 * @date 2026-08-18
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:recruitment-consumer-group}")
    private String consumerGroupId;

    @Bean
    public AdminClient kafkaAdminClient(
            @Value("${spring.kafka.bootstrap-servers}") String servers) {

        Properties properties = new Properties();

        properties.put(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                servers
        );

        return AdminClient.create(properties);
    }

    /**
     * Kafka主题定义
     */
    public static class Topics {
        public static final String RESUME_UPLOAD = "recruitment.resume.upload";           // 简历上传
        public static final String RESUME_PARSED = "recruitment.resume.parsed";           // 简历解析完成
        public static final String RESUME_VECTORIZED = "recruitment.resume.vectorized";   // 简历向量化完成
        public static final String JD_CREATED = "recruitment.jd.created";                 // JD创建
        public static final String CANDIDATE_SCORED = "recruitment.candidate.scored";     // 候选人评分完成
        public static final String RESUME_DLQ = "recruitment.resume.dlq";                 // 死信队列
    }

    // ============Producer配置============

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // 确保消息可靠性
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // 保证顺序
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ============Consumer配置============

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 手动提交offset
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // 手动ACK
        factory.setConcurrency(3); // 并发消费者数量
        return factory;
    }

    // ============Topic自动创建============

    @Bean
    public NewTopic resumeUploadTopic() {
        return TopicBuilder.name(Topics.RESUME_UPLOAD)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic resumeParsedTopic() {
        return TopicBuilder.name(Topics.RESUME_PARSED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic resumeVectorizedTopic() {
        return TopicBuilder.name(Topics.RESUME_VECTORIZED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic jdCreatedTopic() {
        return TopicBuilder.name(Topics.JD_CREATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic candidateScoredTopic() {
        return TopicBuilder.name(Topics.CANDIDATE_SCORED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic resumeDLQTopic() {
        return TopicBuilder.name(Topics.RESUME_DLQ)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
