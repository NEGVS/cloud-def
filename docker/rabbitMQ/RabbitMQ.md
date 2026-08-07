# RabbitMQ
已经在本机docker安装

http://localhost:15672/#/

根据这些配置文件
RabbitMQ 网页客户端需要配置哪些？


# 网页管理后台需要手动创建清单（vhost必须切换到 `andy`）
> ⚠️右上角VHOST下拉框务必选择 **andy**，不要用默认 `/`。
> 两套方案：
> 1. **代码Bean自动声明（推荐）**：网页什么都不用创建，启动项目自动生成所有队列、交换机、绑定、队列arguments。
> 2. **完全网页手动创建**：下面全部手动配置，代码去掉Queue/Exchange/Binding Bean，只保留`@RabbitListener`监听。

你现在用死信+TTL，**重点：主队列的Arguments必须配置死信参数，这是最容易漏的地方**。

## 需要创建的资源清单
### 1.主交换机 andyExchange
- Name：`andyExchange`
- Type：`direct`
- Durability：`Durable`
- Auto‑delete：No
  点击 Add exchange

### 2.主队列 andyQueue（重点！要填Arguments）
Name：`andyQueue`
Durability：`Durable`
Auto‑delete：No

**Arguments框填入下面JSON（复制完整粘贴）**
```json
{"x-message-ttl":60000,"x-dead-letter-exchange":"a_dlx_exchange","x-dead-letter-routing-key":"a_dlx_routing_key"}
```
> 含义：
> - x‑message‑ttl：消息60000ms过期
> - x‑dead‑letter‑exchange：消息死后投递到哪个死信交换机
> - x‑dead‑letter‑routing‑key：死信转发的routingKey

点击 Add queue。

### 3.主绑定：andyExchange → andyQueue
进入 `andyExchange` 交换机详情页 → Add binding from this exchange
- To queue：`andyQueue`
- Routing key：`andyRoutingKey`
- Arguments：**留空**
  点击 Bind。

---

### 4.死信交换机 a_dlx_exchange
Name：`a_dlx_exchange`
Type：`direct`
Durability：`Durable`
Auto‑delete：No
Add exchange

### 5.死信队列 a_dlx_queue
Name：`a_dlx_queue`
Durability：`Durable`
Auto‑delete：No
Arguments：**空**
Add queue

### 6.死信绑定：a_dlx_exchange → a_dlx_queue
进入 `a_dlx_exchange` 详情页 → Add binding from this exchange
- To queue：`a_dlx_queue`
- Routing key：`a_dlx_routing_key`
- Arguments：**留空**
  点击 Bind。

> ✅到此网页全部配置完成。
> 没有单独的delay交换机，你这套是**队列TTL+死信实现延迟，不需要延迟插件，不需要a_delay_exchange/a_delay_queue**；你配置里的delay只是ttl参数，不是独立交换机队列。

## 网页测试验证流程
1. 进入 `andyExchange` → Publish message
2. Routing key：`andyRoutingKey`
3. Payload：`test delay msg`，发布消息
4. 消息进入 `andyQueue`；60秒内不消费，消息消失；
5. 60秒后消息自动进入 `a_dlx_queue`，代表死信延迟链路正常。

## ⚠️高频踩坑
1. vhost忘记切`andy`，全部建到默认`/`，程序启动报错找不到队列。
2. **主队列忘记填Arguments**，死信完全不生效。
3. 绑定的routing‑key大小写、字符必须完全一致，不能多空格。
4. 不要把死信参数填到【binding的Arguments】，**是填在队列的Arguments！！**

## 核对对照表
|资源|名称|类型|特殊Arguments|
|---|---|---|---|
|主交换机|andyExchange|direct|无|
|主队列|andyQueue|queue|`x-message-ttl、x-dead-letter-exchange、x-dead-letter-routing-key`|
|死信交换机|a_dlx_exchange|direct|无|
|死信队列|a_dlx_queue|queue|无|

> 注意：你yaml里的 `delay.exchange、delay.queue` 这两个字段只是业务变量，**这套死信TTL方案并不真正创建这两个mq资源**，不要在网页创建a_delay_exchange、a_delay_queue。

如果你要使用rabbitmq延迟插件的真实延迟交换机，我再给你对应网页配置。

# 1
# 结论：两个方案**二选一，都可以，选其中一套就行，不要混着来**
✅ **方案1（代码Bean自动声明，推荐）：网页完全不用手动创建任何队列、交换机、绑定、arguments。**
你把全套Queue、DirectExchange、Binding Bean写好，yml里vhost配置正确，MQ的`andy`这个vhost已经建好并且账号授权完成。
项目启动的时候，Spring‑AMQP会自动去 `andy` vhost下面：
1. 创建 `andyQueue`，并且自动带上那一套死信arguments（x‑message‑ttl、x‑dead‑letter‑exchange…）
2. 创建 `andyExchange`
3. 自动做好绑定 `andyRoutingKey`
4. 创建死信交换机 `a_dlx_exchange`、死信队列`a_dlx_queue`、死信绑定

> 你打开15672网页，右上角切换vhost=andy，启动完项目刷新页面，就能看到全部资源自动出现。
> ⚠️网页这边**只需要提前建好vhost=andy + 给账号授权，其余什么都不用点**。

> 前提条件（方案1必须先手动做两件事，这不是队列交换机，是vhost层面）
1. rabbitmqctl 创建vhost：`rabbitmqctl add_vhost andy`
2. 授权：`rabbitmqctl set_permissions -p andy admin ".*" ".*" ".*"`

> 队列、交换机、binding、arguments全部交给代码生成。

---

## 方案2：网页全部手动建
网页上把上面所有交换机、队列、arguments、绑定全部手工配置完成。
**Java代码要删掉所有 @Bean 的Queue / Exchange / Binding，只保留 @RabbitListener消费者监听。**
> 如果Bean还留在代码，启动会重复声明，大部分情况没问题，但不建议混用。

## ❌绝对不要做的事：混合操作
> 比如：网页手动建了andyQueue，代码又写了andyQueue的Bean。
- 如果两边参数完全一样（durable、arguments）：没事；
- 如果两边参数不一致（比如网页没写死信arguments，代码里写了）：**启动直接报错，队列已存在但属性不匹配 `inequivalent arg 'x‑dead‑letter‑exchange' for queue`**。
> 这是非常高频的线上bug。

## 简单一句话总结
1. **方案1（推荐）：只提前建好vhost+权限，队列交换机全部代码管，网页不用手动建队列交换机。**
2. **方案2：全部网页手搓，代码删除所有MQ声明Bean，只留监听消费。**

> 生产环境优先方案1：所有MQ资源定义在代码，纳入版本管理，部署到不同环境自动创建，不用人工操作网页。

### 小提醒
如果代码Bean已经创建过队列，后续修改队列的arguments（比如改ttl时间）：
队列一旦创建完成，**不能在线修改参数**。需要网页删除队列，重启项目，才会应用新的arguments。


# 自动配置
# RabbitMQ 死信+延迟TTL完整配置
> 说明：这里用**队列TTL死信实现延迟效果**（不需要安装delay插件）；主队列消息超时后转发到死信交换机，进入死信队列。
参数汇总：
**主队列**
- queue: `andyQueue`
- exchange: `andyExchange`
- routing‑key: `andyRoutingKey`

**死信DLX**
- dlx‑exchange: `a_dlx_exchange`
- dlx‑routing‑key: `a_dlx_routing_key`
- dlx‑queue: `a_dlx_queue`

**TTL：主队列消息过期时间 60000ms = 60秒**

>原理：消息进入`andyQueue`，60s没被消费，消息死亡，投递到死信交换机`a_dlx_exchange`，通过`a_dlx_routing_key`路由到死信队列`a_dlx_queue`，业务监听死信队列做延迟逻辑。

## 1.YAML常量（便于维护）
```yaml
mq:
  main:
    queue: andyQueue
    exchange: andyExchange
    routing-key: andyRoutingKey
  dlx:
    exchange: a_dlx_exchange
    routing-key: a_dlx_routing_key
    queue: a_dlx_queue
  delay:
    ttl: 60000
```

## 2.Java配置类完整代码
```java
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitDlxConfig {

    // 主队列
    @Value("${mq.main.queue}")
    private String mainQueue;
    @Value("${mq.main.exchange}")
    private String mainExchange;
    @Value("${mq.main.routing-key}")
    private String mainRoutingKey;

    // 死信
    @Value("${mq.dlx.exchange}")
    private String dlxExchange;
    @Value("${mq.dlx.routing-key}")
    private String dlxRoutingKey;
    @Value("${mq.dlx.queue}")
    private String dlxQueue;

    @Value("${mq.delay.ttl}")
    private Integer ttl;


    // ========== 主队列：绑定死信交换机 + 设置消息TTL ==========
    @Bean
    public Queue andyQueue() {
        return QueueBuilder.durable(mainQueue)
                // 设置该队列消息过期时间
                .withArgument("x-message-ttl", ttl)
                // 指定死信交换机
                .withArgument("x-dead-letter-exchange", dlxExchange)
                // 指定死信转发时使用的routing‑key
                .withArgument("x-dead-letter-routing-key", dlxRoutingKey)
                .build();
    }

    // 主交换机 direct
    @Bean
    public DirectExchange andyExchange() {
        return ExchangeBuilder.directExchange(mainExchange).durable(true).build();
    }

    // 主绑定
    @Bean
    public Binding mainBinding(Queue andyQueue, DirectExchange andyExchange) {
        return BindingBuilder
                .bind(andyQueue)
                .to(andyExchange)
                .with(mainRoutingKey);
    }

    // ========== 死信交换机、死信队列、死信绑定 ==========
    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(dlxExchange).durable(true).build();
    }

    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(dlxQueue).build();
    }

    @Bean
    public Binding dlxBinding(Queue dlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder
                .bind(dlxQueue)
                .to(dlxExchange)
                .with(dlxRoutingKey);
    }
}
```

## 3.消费者示例
```java
@Component
public class MqConsumerDemo {

    // 消费主业务队列
    @RabbitListener(queues = "${mq.main.queue}")
    public void consumeMain(String msg, Channel channel, Message message) throws Exception {
        try {
            System.out.println("主队列收到：" + msg);
            // 业务处理
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    // 消费死信队列
```
