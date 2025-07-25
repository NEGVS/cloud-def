package xCloud.feignClient.productClient;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;

import java.io.IOException;

/**
 * @Description 创建发送普通消息程序并运行
 * @Author Andy Fan
 * @Date 2025/2/28 11:45
 * @ClassName ProducerExample
 */
@Slf4j
public class ProducerExample {
    public static void main(String[] args) throws ClientException, IOException {
        // 接入点地址，需要设置成Proxy的地址和端口列表，一般是xxx:8080;xxx:8081
        // 此处为示例，实际使用时请替换为真实的 Proxy 地址和端口
//        String endpoint = "172.17.0.2:9876";
        String endpoint = "rmq-namesrv:9876";
        // 消息发送的目标Topic名称，需要提前创建。
        String topic = "TestTopic";
        ClientServiceProvider provider = ClientServiceProvider.loadService();

        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);

        ClientConfiguration configuration = builder.build();
//        初始化Producer时需要设置通信配置以及预绑定的Topic。

        Producer producer = provider.newProducerBuilder()
                .setTopics(topic)
                .setClientConfiguration(configuration)
                .build();
        //普通消息发送
        Message message = provider.newMessageBuilder()
                .setTopic(topic)
                // 设置消息索引键，可根据关键字精确查找某条消息。
                .setKeys("messageKey")
                // 设置消息Tag，用于消费端根据指定Tag过滤消息
                .setTag("messageTag")
                // 消息体。
                .setBody("Hello world!".getBytes())
                .build();
        try {
            //发送消息，需要关注发送结果，并捕获失败等异常。
            SendReceipt sendReceipt = producer.send(message);
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        } catch (ClientException e) {
            log.info("Send message failed, Topic: {}, Exception: {}", topic, e);
        }
        producer.close();
    }
}
