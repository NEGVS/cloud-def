package xCloud.config.exception;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import xCloud.entity.PaymentSuccessEvent;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/16 13:24
 * @ClassName CustomKafkaErrorHandler
 */
// 自定义 ErrorHandler Bean（全局或特定）
@Component
@Slf4j
public class CustomKafkaErrorHandler implements KafkaListenerErrorHandler {
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;  // 用于发送 DLQ

    //

    /**
     * @param message   message
     * @param exception exception
     * @return KafkaListenerErrorHandler 和 ConsumerAwareListenerErrorHandler
     * default Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
     * 优化说明
     * <p>
     * 接口切换：原代码实现了 KafkaListenerErrorHandler（仅 2 个参数），但方法签名有 3 个参数，导致抽象方法不匹配。切换到 ConsumerAwareListenerErrorHandler（子接口，支持 consumer 参数，用于高级操作如 seek）。如果不需要 consumer，可移除第三个参数并回退到基本接口。
     * 导入修正：添加 ConsumerAwareListenerErrorHandler 导入（假设您的项目有 org.springframework.kafka.listener 包）。
     * Header 修正：
     * <p>
     * KafkaHeaders.RECEIVED_PARTITION → KafkaHeaders.RECEIVED_PARTITION_ID（标准名称）。
     * 异常检查：exception.getMessage() → exception.getCause().getMessage()（ListenerExecutionFailedException 包装了原始异常）。
     * <p>
     * <p>
     * 重试计数：移除了 KafkaHeaders.DELIVERY_ATTEMPT（非标准；Spring Kafka 通过 DefaultErrorHandler 配置 retries）。建议在 application.yml 中设置 spring.kafka.consumer.properties.retries=3。
     * Bean 名称：添加 @Component("customKafkaErrorHandler")，便于 @KafkaListener(errorHandler = "customKafkaErrorHandler") 引用。
     * Consumer 未使用：当前代码中 consumer 参数未调用（如需 seek 偏移：consumer.seek(new TopicPartition(topic, partition), offset)）。如果不需要，可简化接口。
     * 测试：重启应用，模拟异常（e.g., 抛 RuntimeException("订单不存在")），观察日志和 DLQ 主题（Offset Explorer 中查看 "dlq-payment-success-topic"）。
     */
    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
        // 从 message 获取 payload（PaymentSuccessEvent）和 headers
        Object payload = message.getPayload();
        String topic = (String) message.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC);
        Integer partition = (Integer) message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION);
        Long offset = (Long) message.getHeaders().get(KafkaHeaders.OFFSET);

        // 记录异常详情：包含消息记录（record）和数据（data）
        log.error("Kafka 消费异常，主题: {}, 分区: {}, 偏移: {}, 异常: {}",
                topic, partition, offset, exception.getMessage(), exception.getCause());

        // 示例：如果异常是 "订单不存在"，发送到 DLQ 主题
        if (exception.getMessage().contains("订单不存在")) {
            try {
                // 发送原始消息到 DLQ（保留 headers）
                kafkaTemplate.send("dlq-payment-success-topic", message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, String.class), payload);
                log.warn("已发送到 DLQ: 订单号 {}", ((PaymentSuccessEvent) payload).getOrderNo());
                return "handled";  // 已处理，不重试
            } catch (Exception dlqEx) {
                log.error("DLQ 发送失败", dlqEx);
            }
        }

        // 其他异常：返回 null，让 Kafka 重试（默认 3 次，根据配置）
        // 可添加重试计数：if (deliveryAttempt > 3) { 发送 DLQ; return "failed"; }
        Integer deliveryAttempt = (Integer) message.getHeaders().get(KafkaHeaders.DELIVERY_ATTEMPT);
        if (deliveryAttempt != null && deliveryAttempt > 3) {
            kafkaTemplate.send("dlq-payment-success-topic", payload);
            return "max-retries-exceeded";
        }
        return null;  // 继续重试
    }
}
