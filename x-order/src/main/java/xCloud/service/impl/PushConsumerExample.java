package xCloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;

import java.util.Collections;

/**
 * @Description 创建订阅普通消息程序并运行
 * @Author Andy Fan
 * @Date 2025/2/28 14:13
 * @ClassName PushConsumerExample
 */
@Slf4j
public class PushConsumerExample {

    public static void main(String[] args) throws Exception {

        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        //接入点地址，需要设置成Proxy的地址和端口列表，一般是xxx:8080;xxx:8081
        // 此处为示例，实际使用时请替换为真实的 Proxy 地址和端口 是
        String endpoint = "127.0.0.1:9876";
        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
                .setEndpoints(endpoint)
                .build();
        //订阅消息的过滤规则，表示订阅所有Tag的消息
        String tag = "*";
        FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);
        // 为消费者指定所属的消费者分组，Group需要提前创建。
        String consumerGroup = "TestGroup";
        // 指定需要订阅哪个目标Topic，Topic需要提前创建
        String topic = "TestTopic";
        // 初始化PushConsumer，需要绑定消费者分组ConsumerGroup、通信参数以及订阅关系
        PushConsumer pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(clientConfiguration)
                .setConsumerGroup(consumerGroup)
                .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression))
                .setMessageListener(messageView -> {
                    log.info("Receive message, messageId={}, messageBody={}, topic={}, tag={}, key={}, consumerGroup={}", messageView.getMessageId());
                    return ConsumeResult.SUCCESS;
                }).build();
        Thread.sleep(Long.MAX_VALUE);
        // 如果不需要再使用 PushConsumer，可关闭该实例。
        pushConsumer.close();


    }


}
