package xcloud.xproduct.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import xcloud.xproduct.domain.XProducts;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/4/3 15:26
 * @ClassName KafkaProducer
 */
@Service
@Slf4j
public class KafkaProducer {

    //kafka topic--TOPIC 变量定义了你要发送消息的 Kafka Topic 名称。
    private static final String TOPIC = "andy_topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaTemplate<String, XProducts> productsKafkaTemplate;

    public void sendProductMessage(XProducts product) {
        log.info("topic:{}", TOPIC);
        log.info("Kafka Producer send message: {}", product);
        this.productsKafkaTemplate.send(TOPIC, product);
    }


    /**
     * sendMessage 方法使用 kafkaTemplate.send(topic, message) 发送不带键的消息
     *
     * @param message message
     */
    public void sendMessage(String message) {
        log.info("\n----Kafka Producer send \nmessage: {}", message + "\ntopic:{}", TOPIC);
        this.kafkaTemplate.send(TOPIC, message);
    }

    /**
     * sendMessageWithKey 方法使用 kafkaTemplate.send(topic, key, message) 发送带有键的消息。
     *
     * @param key     key
     * @param message message
     */
    public void sendMessageWithKey(String key, String message) {
        log.info(String.format("Sending message to %s with key %s: %s", TOPIC, key, message));
        this.kafkaTemplate.send(TOPIC, key, message);
    }
}
