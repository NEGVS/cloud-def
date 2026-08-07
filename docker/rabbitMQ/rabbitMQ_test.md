# 完整测试代码
> 前提：yml已经修复层级，项目启动成功；@Bean会自动创建：
- 主：`andyQueue` / `andyExchange` / `andyRoutingKey`
- 死信：`a_dlx_queue` / `a_dlx_exchange` / `a_dlx_routing_key`
- 延迟：`a_delay_queue` / `a_delay_exchange` / `a_delay_routing_key`

> 业务逻辑：
1. 主队列消息，如果**正常消费成功 → ack，消息消失**
2. 主队列消息，**业务异常抛出 → nack(false,false)，消息不回原队列，进入死信交换机，落到 a_dlx_queue**
3. 主队列设置了 `x‑message‑ttl=60000`：消息在主队列放够60s没有被消费，自动过期投递到死信队列。
4. 你额外一套独立 delayQueue/delayExchange 是另一套普通队列，和TTL过期无关。

## 1、生产者（测试发送消息，直接写单元测试）
```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@SpringBootTest
public class MqProducerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // ==========测试1：发送消息到主队列andyExchange，正常消息==========
    @Test
    void sendNormalMessage() {
        String msg = "测试正常消息-" + System.currentTimeMillis();
        rabbitTemplate.convertAndSend("andyExchange", "andyRoutingKey", msg);
        System.out.println("发送消息：" + msg);
    }

    // ==========测试2：发送消息，不启动消费者，让消息在主队列放满60s自动TTL过期进死信==========
    /**
     * 操作：运行这个发送，注释掉主队列@RabbitListener，不消费；
     * 等待60秒，消息自动过期，自动进入 a_dlx_queue死信队列
     */
    @Test
    void sendTtlTestMessage() {
        String msg = "TTL过期测试消息，60秒后进死信-" + System.currentTimeMillis();
        rabbitTemplate.convertAndSend("andyExchange", "andyRoutingKey", msg);
        System.out.println("发送TTL测试消息：" + msg);
    }

    // ==========测试3：发送到你独立的延迟交换机 a_delay_exchange==========
    @Test
    void sendDelayMsg() {
        String msg = "独立delay队列消息-" + System.currentTimeMillis();
        rabbitTemplate.convertAndSend("a_delay_exchange", "a_delay_routing_key", msg);
        System.out.println("发送delay队列消息：" + msg);
    }
}
```

## 2、消费者完整代码（主队列 + 死信队列两套，manual ack）
```java
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MqConsumer {

    /**
     * 主队列消费者 andyQueue
     */
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void receiveMain(String message, Channel channel, Message amqpMessage) {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            log.info("【主队列接收到消息】：{}", message);

            //===================== 这里可以人为制造异常，测试死信流程 =====================
            // 放开下面注释，模拟业务异常，消息nack，进入死信队列
            // int i = 1 / 0;

            //正常业务处理
            processBusiness(message);

            //手动ack确认成功
            channel.basicAck(deliveryTag, false);
            log.info("【主队列消息ack成功】");

        } catch (Exception e) {
            log.error("【主队列消费异常】 message={},error={}", message, e.getMessage(), e);
            try {
                // nack，requeue=false，不重回原队列，进入死信；无DLX则消息丢弃
                channel.basicNack(deliveryTag, false, false);
                log.info("【消息已nack，投递死信队列】");
            } catch (IOException ex) {
                log.error("nack调用失败", ex);
            }
        }
    }

    /**
     * 死信队列 a_dlx_queue 消费者
     */
    @RabbitListener(queues = "${rabbitmq.dlx.queue}")
    public void receiveDlx(String message, Channel channel, Message amqpMessage) {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            log.warn("====【死信队列收到消息】====：{}", message);
            //死信业务：告警、落库、人工补偿逻辑
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("死信队列消费失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("死信nack异常", ex);
            }
        }
    }

    /**
     * 独立delay队列 a_delay_queue消费者
     */
    @RabbitListener(queues = "${rabbitmq.delay.queue}")
    public void receiveDelay(String message, Channel channel, Message amqpMessage) {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            log.info("【独立delay队列收到消息】：{}", message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("delay队列消费异常", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("delay nack异常", ex);
            }
        }
    }


    private void processBusiness(String message) {
        //你的业务逻辑
    }
}
```

# 🧪3套场景测试步骤
## 场景1：正常消费，ack成功，消息消失
1. 启动完整项目，主、死信、delay消费者全部开启
2. 运行单元测试 `sendNormalMessage()`
3. 看日志：主队列打印收到消息，ack成功；网页看andyQueue消息计数变为0。

## 场景2：业务异常，消息nack进入死信队列
1. 打开代码，放开 `int i = 1 / 0;` 模拟业务报错
2. 运行 `sendNormalMessage()`
3. 日志：主队列捕获异常，执行basicNack(false,false)
4. 消息不会留在andyQueue；**死信消费者打印消息，代表死信链路通了**。

> 测试完记得把`int i =1/0;`注释掉。

## 场景3：消息在主队列60s没人消费，TTL过期自动进死信
1. **注释掉主队列的@RabbitListener，关闭主队列消费者**（不消费消息）
2. 运行单元测试 `sendTtlTestMessage()`，发送消息到andyQueue
3. 打开网页管理页面vhost=andy，andyQueue里面可以看到消息；
4. **等待60秒**，消息从andyQueue消失，自动转发到`a_dlx_queue`；死信消费者打印这条消息。
> ✅验证主队列TTL和DLX死信配置生效。

## 场景4：独立delay队列测试
运行 `sendDelayMsg()`，`receiveDelay`打印消息，这套是普通队列，**没有TTL过期逻辑**。

# 网页管理页面观察指标（http://127.0.0.1:15672/#/queues/%2Fandy）
切换vhost=andy：
- `andyQueue`：消息数、unacked未确认消息；
- `a_dlx_queue`：死信消息堆积；
> 如果manual ack忘记写ack/nack，unacked一直上涨，消息不会消失。

# 重要踩坑提示
1. manual模式下**必须执行ack或者nack，否则消息一直unacked堆积**。
2. 主队列TTL过期、basicNack(false,false)两种情况都会把消息送入死信交换机。
3. 如果你想重试，不要直接nack(requeue=true)，会无限循环消费；建议做本地重试次数再打入死信。

# 问题排查
1. 消息没进死信：打开网页看andyQueue队列详情，确认队列是否带有`x‑message‑ttl`、`x‑dead‑letter‑exchange`参数；
2. 如果没有，代表@Bean没有创建成功，删除队列重启项目。
