# 代码点评&问题修正
你的逻辑大体没问题，但有**2处bug、小优化点**：
1. catch块里嵌套捕获，里面打印日志用了外层`e`，不是内层`ex`；
2. 方法声明抛出没有`throws Exception`，`channel.basicAck / basicNack`会抛出`IOException`，虽然你写了try‑catch，但方法签名最好规范；
3. 变量名：参数`Message amqpMessage`，和import的`org.springframework.amqp.core.Message`类名重名，容易混淆。

> 关键点：`channel.basicNack(tag,false,false)` → **不会重新入队，消息会被丢弃**。
> ⚠️注意：**只有主队列andyQueue配置了x‑dead‑letter‑exchange，被nack的消息才会进入死信队列a_dlx_queue**；如果队列没有配置DLX，消息直接丢失。

## 修正后完整版本
```java
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import java.io.IOException;

@Slf4j
@Component
public class MqConsumer {

    public static final String QUEUE_NAME = "${rabbitmq.queue}";

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void receiveMessage(String message, Channel channel, Message amqpMessage) {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            log.info("\n接收到消息：{}", message);
            processMessage(message);
            // manual ack 确认成功
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("\n处理消息出错：{}", e.getMessage(), e);
            log.error("处理消息失败，队列 [{}],消息内容: {}", QUEUE_NAME, message, e);
            try {
                // false,false：不批量、不重新入队；配置了DLX则消息进入死信队列，否则直接丢弃
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("\n拒绝消息发生异常：{}", ex.getMessage(), ex);
            }
        }
    }

    private void processMessage(String message) {
        // 你的业务逻辑
    }
}
```

## 重要说明
1. **`basicNack(deliveryTag, false, false)`行为**
- 当前`andyQueue`配置了`x‑dead‑letter‑exchange`死信参数，消息nack之后，**消息会转发到死信交换机，进入a_dlx_queue** ✅
- 如果某一天把队列死信参数删掉，nack之后消息直接丢掉，务必注意。

2. 参数requeue的两个选项
```java
// 第三个参数 true：消息打回原队列，会无限循环消费，业务异常不要随便开
// channel.basicNack(deliveryTag, false, true);
```

3. yml必须确认
```yaml
spring:
  rabbitmq:
    listener:
      simple:
        acknowledge-mode: manual
```
> 只要ack模式是manual，**必须执行ack/nack，否则消息一直挂在队列，不会消失**。

## 启动前检查清单
1. yml中`rabbitmq.queue`的值等于`andyQueue`；
2. vhost=andy，权限已经授权；
3. 网页已经删除旧的andyQueue、andyExchange，全部交给@Bean创建；
4. 死信队列`a_dlx_queue`也需要写一套同样manual ack的消费者，否则死信消息堆积。

## 潜在风险提示
- 业务异常就直接nack到死信：需要人工消费死信队列做补偿、排查；
- 如果只是临时波动（网络、数据库超时），建议可以有限次数重试，再打入死信，不然正常业务抖动直接进死信。

> 现在这套代码可以直接启动运行。
> 如果启动报`internalRabbitListenerEndpointRegistry`，优先检查yml的`acknowledge-mode:manual`不要写错，不要有多余空格。
