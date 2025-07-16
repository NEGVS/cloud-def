package xcloud.xproduct.config.kafka;


import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import xcloud.xproduct.domain.XProducts;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/4/3 15:33
 * @ClassName KafkaConsumer
 */
@Component
@Slf4j
public class KafkaConsumer {

    /**
     * 监听指定 andy_topic 的消息
     *
     * @KafkaListener 注解来监听指定的 Kafka Topic 并消费消息
     */
    @KafkaListener(topics = "andy_topic", groupId = "andy_group")
    public void consume(String message) {
        log.info("\n--------Kafka Consumer message: {}", message);
    }

    /**
     * 监听指定 another-topic 的消息
     * @param message message
     */
    @KafkaListener(topics = "another-topic", groupId = "another-group")
    public void consumeFromAnotherTopic(String message) {
        log.info("\nKafka Consumed message from another-topic: %s%n", message);
    }

    /**
     * consumeWithKey 方法演示了如何消费带有键的消息，并通过 properties 属性覆盖了默认的键反序列化器。
     *
     * @param message message
     * @param key     key
     */
    @KafkaListener(topics = "my-topic", groupId = "my-group", properties = {"key.deserializer=org.apache.kafka.common.serialization.StringDeserializer"})
    public void consumeWithKey(String message, String key) {
        log.info("\nKafka Consumed message with key %s: %s%n", key, message);
    }

    //-------可以消费对象-------
    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void consumeUser(XProducts user) {
        log.info(String.format("Consumed user: %s", user));
    }

//    @Bean
//    public KafkaListenerContainerFactory<?> productKafkaListenerContainerFactory(ConsumerFactory<String, XProducts> consumerFactory) {
//        ConcurrentKafkaListenerContainerFactory<String, XProducts> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory);
//        return factory;
//    }


}
