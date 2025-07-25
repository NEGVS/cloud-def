package xcloud.xproduct.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @Description test grok ,
 * Spring Kafka 的生产者和消费者示例，发送和接收 JSON 消息
 * 生产者：使用 KafkaTemplate 发送消息，带回调确认送达。
 * 消费者：通过 @KafkaListener 注解监听指定主题，自动消费消息。
 * Spring Boot 集成：利用 Spring Kafka 简化配置，自动管理消费者组和 offset。
 * @Author Andy Fan
 * @Date 2025/5/27 19:51
 * @ClassName KafkaService
 */
@Slf4j
@Service
public class KafkaService {
    private static final String TOPIC = "test_topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送消息
     *
     * @param message message
     */
    public void sendMessage(String message) {
        log.info("\n开始发送消息：" + message);
        kafkaTemplate.send(TOPIC, message).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("\nsend message success ,topic " + result.getRecordMetadata().topic() +
                        " partition: " + result.getRecordMetadata().partition());
            } else {
                log.error("\nsend message failed: " + ex.getMessage());
            }
        });
    }

    /**
     * 监听消息
     *
     * @param message message
     */
    @KafkaListener(topics = "test_topic", groupId = "test_group")
    public void consumeMessage(String message) {
        log.info("\nKafka Consumers receive messages：" + message);
    }
}
