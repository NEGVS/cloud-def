package xcloud.xproduct.config.kafka;

//import com.alibaba.nacos.shaded.com.google.gson.JsonDeserializationContext;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Component;
import xcloud.xproduct.domain.XProducts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/4/3 15:33
 * @ClassName KafkaConsumer
 */
@Component
@Slf4j
public class KafkaConsumer {


    //@KafkaListener 注解来监听指定的 Kafka Topic 并消费消息
    @KafkaListener(topics = "andy_topic", groupId = "andy_group")
    public void consume(String message) {
        log.info("\n--------Consumer message: {}", message);
    }

    @KafkaListener(topics = "another-topic", groupId = "another-group")
    public void consumeFromAnotherTopic(String message) {
        log.info("Consumed message from another-topic: %s%n", message);
    }

    /**
     * consumeWithKey 方法演示了如何消费带有键的消息，并通过 properties 属性覆盖了默认的键反序列化器。
     *
     * @param message message
     * @param key     key
     */
    @KafkaListener(topics = "my-topic", groupId = "my-group", properties = {"key.deserializer=org.apache.kafka.common.serialization.StringDeserializer"})
    public void consumeWithKey(String message, String key) {
        log.info("Consumed message with key %s: %s%n", key, message);
    }

    //    -------可以消费对象-------
    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void consumeUser(XProducts user) {
        System.out.println(String.format("Consumed user: %s", user));
    }

//    @Bean
//    public KafkaListenerContainerFactory<?> productKafkaListenerContainerFactory(ConsumerFactory<String, XProducts> consumerFactory) {
//        ConcurrentKafkaListenerContainerFactory<String, XProducts> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory);
//        return factory;
//    }


}
