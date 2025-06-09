package xCloud.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xCloud.service.RabbitMQService;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/5/28 15:09
 * @ClassName RabbitMQConfig
 * 主队列通过 x-dead-letter-exchange 和 x-dead-letter-routing-key 配置死信交换机和路由键。
 * 使用 QueueBuilder 创建持久化队列（durable=true），确保消息和队列在 RabbitMQ 重启后不丢失。
 * DirectExchange 用于精确路由消息。
 * <p>
 * - 主队列监听：使用 @RabbitListener(queues = "#{@messageService.queueName}") 解决常量问题。
 * - 死信队列监听：为死信队列添加单独的监听器 receiveDeadLetterMessage，处理失败的消息。
 * - 消息拒绝：在 receiveMessage 中，处理失败时调用 basicNack 并设置 requeue=false，将消息发送到死信队列。
 */
@Configuration
public class RabbitMQConfig {


    /**
     * 主要队列
     */

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.queue}")
    private String queueName;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    /**
     * 死信队列
     */

    @Value("${rabbitmq.dlx.exchange}")
    private String dlxExchangeName;

    @Value("${rabbitmq.dlx.queue}")
    private String dlxQueueName;

    @Value("${rabbitmq.dlx.routing-key}")
    private String dlxRoutingKey;

    /**
     * 延迟队列
     */

    @Value("${rabbitmq.delay.exchange}")
    private String delayExchangeName;

    @Value("${rabbitmq.delay.queue}")
    private String delayQueueName;

    @Value("${rabbitmq.delay.routing-key}")
    private String delayRoutingKey;

    @Value("${rabbitmq.delay.ttl}")
    private int delayTtl;

    /**
     * main exchange
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    /**
     * dlxExchange
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    /**
     * delayExchange
     */
    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(delayExchangeName, true, false);
    }
    /**
     * main queue,binding dlxExchange and dlxRoutingKey
     * 死信触发场景 ok
     * 消息会进入死信队列的情况包括：
     * - 消息被消费者拒绝（basicNack 或 basicReject 且 requeue=false）。
     * - 消息过期（需设置 x-message-ttl）。
     * - 队列达到最大长度（需设置 x-max-length）。
     */
    @Bean
    public Queue queue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", dlxRoutingKey)
                .withArgument("x-max-length", 10) // 队列长度限制
                .withArgument("x-message-ttl", 10000)  //  消息存活时间（毫秒）
                .build();
    }

    /**
     * dlxQueue
     */
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(dlxQueueName).build();
    }


    /**
     * 延迟队列，设置TTL和死信路由
     * 延迟队列 my.delay.queue 配置了 x-message-ttl，消息在队列中存活指定时间后过期。
     * 过期消息通过 x-dead-letter-exchange 和 x-dead-letter-routing-key 路由到目标队列 my.target.queue。
     */
    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(delayQueueName)
                .withArgument("x-dead-letter-exchange", exchangeName) // 死信交换机
                .withArgument("x-dead-letter-routing-key", routingKey) // 死信路由键
                .withArgument("x-message-ttl", delayTtl) // 消息TTL
                .build();
    }

    /**
     * main queue binding main exchange and routingKey
     */
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    /**
     * dlxQueue binding dlxExchange and dlxRoutingKey
     */
    @Bean
    public Binding dlxBinding(Queue dlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(dlxQueue).to(dlxExchange).with(dlxRoutingKey);
    }

    /**
     * delay queue binding 延迟队列绑定
     */
    @Bean
    public Binding delayBinding(Queue delayQueue, DirectExchange delayExchange) {
        return BindingBuilder.bind(delayQueue).to(delayExchange).with(delayRoutingKey);
    }

}
