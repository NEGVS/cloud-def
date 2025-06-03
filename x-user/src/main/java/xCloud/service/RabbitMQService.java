package xCloud.service;

import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/5/28 09:41
 * @ClassName RabbitMQService
 */
@Service
@Slf4j
public class RabbitMQService {
    // 从配置文件读取队列和交换机名称
    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.queue}")
    private String QUEUE_NAME;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    @Value("${rabbitmq.dlx.queue}")
    private String dlxQueueName;

    /**
     * delay
     */
    @Value("${rabbitmq.delay.exchange}")
    private String delayExchangeName;

    @Value("${rabbitmq.delay.queue}")
    private String delayQueueName;

    @Value("${rabbitmq.delay.routing-key}")
    private String delayRoutingKey;


    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 项目启动后执行
     */
    @PostConstruct
    public void init() {
        log.info("\ninit 队列名称: {}", QUEUE_NAME);
    }

    /**
     * 发送消息
     */
    public void sendMessage(String message) {
        //  发送持久化消息
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message, msg -> {
            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            msg.getMessageProperties().setMessageId(String.valueOf(System.currentTimeMillis()));
            return msg;
        });
        log.info("\n\n发送消息：" + message);
    }

    /**
     * 发送消息到延迟队列
     * sendDelayedMessage：发送消息到延迟队列，消息在 TTL 过期后路由到目标队列。
     * receiveMessage：监听目标队列，处理延迟后的消息。
     * 使用 SpEL #{@messageService.targetQueueName} 解决 @RabbitListener 的常量问题。
     * 手动确认（basicAck）确保消息处理成功。
     * 如果延迟消息处理失败，可以进一步路由到死信队列（参考前文）。以下是扩展配置：
     */
    public void sendDelayedMessage(String message, long ttlMillis) {
        try {
            rabbitTemplate.convertAndSend(delayExchangeName, delayRoutingKey, message, msg -> {
                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                // 动态TTL,使用 setExpiration 设置消息级别的 TTL，覆盖队列的默认 TTL。
                msg.getMessageProperties().setExpiration(String.valueOf(ttlMillis));
                return msg;
            });
//            log.info("发送延迟消息到队列 [{}]，路由键 [{}]: {}", delayQueueName, delayRoutingKey, message);
//            log.info("\n发送延迟消息到队列 [{}]，路由键 [{}]: {}", delayQueueName, delayRoutingKey, message );
            log.info("\n发送延迟消息到队列 [{}]，路由键 [{}]: {}, ttlMillis:{}", delayQueueName, delayRoutingKey, message, ttlMillis);
        } catch (Exception e) {
            log.error("发送延迟消息失败: {}", message, e);
        }
    }

    /**
     * 接收消息--监听目标队列，处理延迟后的消息,
     * 使用 #{} 语法在 @RabbitListener 中引用 SpEL 表达式。
     * queueName 是类中的字段，通过 @Value 注入。
     *
     * @param message
     * @messageService 是 Spring 容器中该 bean 的名称（默认是类名首字母小写）。确保类标注了 @Component 或其他 Spring 管理的注解。
     * @messageService 是 Spring 容器中该 bean 的名称（默认是类名首字母小写）。确保类标注了 @Component 或其他 Spring 管理的注解。
     */
//    @RabbitListener(queues = QUEUE_NAME)
    @RabbitListener(queues = "${rabbitmq.queue}")
//    @RabbitListener(queues = "#{@RabbitMQService.QUEUE_NAME}")
    public void receiveMessage(String message, Channel channel, Message amqpMessage) {
        try {
            log.info("\n接收到消息：{}", message);
            processMessage(message);
            //manual  ack
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("\n处理消息出错：{}", e.getMessage());
            log.error("处理延迟消息失败，从队列 [{}]: {}", QUEUE_NAME, message, e);
            try {
                // 拒绝消息并重新入队（可根据需求调整为死信队列）
                //channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, true);
                //拒绝消息并发送到死信队列（不重新入队）
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("\n消息处理失败，拒绝消息：{}", e.getMessage());
            }
        }
    }

    /**
     * 接收消息死信
     *
     * @param message
     * @param channel
     * @param amqpMessage
     */
    @RabbitListener(queues = "${rabbitmq.dlx.queue}")
    public void receiveDeadLetterMessage(String message, Channel channel, Message amqpMessage) {
        try {
            log.info("\n接收到死信消息：{}", message);
            processDeadLetterMessage(message);
            //manual ack
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("\n处理死信消息出错：{}", e.getMessage());
            try {
                //可选择丢弃或进一步处理
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ex) {
                log.error("\n处理死信消息失败，拒绝消息：{}", e.getMessage());
            }
        }
    }

    private void processDeadLetterMessage(String message) {
        // 示例业务逻辑
        log.debug("\n处理死信消息：{}", message);
    }


    private void processMessage(String message) {
        // 示例业务逻辑
        log.debug("\n处理消息：{}", message);
    }

    private void processDelayMessage(String message) {
        // 示例业务逻辑
        log.debug("\n处理延迟消息: {}", message);
    }
}
