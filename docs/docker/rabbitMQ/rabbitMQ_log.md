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


# 进入docker里的mq
# 关键问题：命令要进到docker容器内部执行！
你现在是宿主机mac终端执行 `rabbitmqctl`，**本机没有装rabbitmq，命令无效**，权限设置全部没生效！
RabbitMQ跑在docker容器 `rabbitmq`（id:960d8f15693a），所有`rabbitmqctl`命令**必须进入容器里面跑**。

## 1、进入rabbitmq容器内部终端
```bash
docker exec -it rabbitmq bash
```
进入容器后，才可以执行 rabbitmqctl 命令。

## 2、容器内执行：创建vhost、授权admin账号访问andy
> 如果vhost `andy` 还没创建，容器内执行：
```bash
rabbitmqctl add_vhost andy
```

给admin用户授予andy vhost全部权限（容器内执行）
```bash
rabbitmqctl set_permissions -p andy admin ".*" ".*" ".*"
```

查看andy vhost权限确认
```bash
rabbitmqctl list_permissions -p andy
```
✅正常输出：
```
admin	.*	.*	.*
```

查看该用户全部权限
```bash
rabbitmqctl list_user_permissions admin
```
退出容器 `exit`。

## 3、你的yml配置确认（docker映射端口没问题）
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123
    virtual-host: andy
    publisher-returns: true
    publisher-confirm-type: correlated
    listener:
      simple:
        acknowledge-mode: manual
```
> 端口映射：`0.0.0.0:5672->5672/tcp`，宿主机localhost:5672可以访问，没问题。
> 网页地址：http://127.0.0.1:15672，登录账号admin/admin123，登录后右上角切换vhost下拉框，可以看到`andy`。

## 4、完整排查逻辑
1. mac宿主机执行`rabbitmqctl`是无效的，命令不在宿主机，在docker容器内部。
2. 之前你虽然以为设置了权限，实际根本没生效，所以Java客户端连接vhost=andy直接报 `ACCESS_REFUSED`。
3. 容器内执行完上面授权命令，重启springboot项目即可。

### 验证小技巧
浏览器打开 http://127.0.0.1:15672，登录成功后，右上角vhost下拉列表，**能看到andy**，代表vhost创建成功。

> 如果网页右上角看不到andy：说明add_vhost命令没执行成功，重新进容器操作。

做完上面步骤，再启动springboot；如果依旧报错，把容器内`list_user_permissions admin`输出给我。




诊断 100% 坐实了。真相和你 git diff 里改密码的方向不完全一样，我说清楚：

Docker 里的真实情况（刚查的）：
- 账号只有 admin / admin123，没有 guest 用户
- vhost 有 / 和 andy，admin 对两者都有全权限

根因：application.yml 里的 rabbitmq: 是顶级节点，不在 spring: 下。Spring Boot 的 AMQP 自动配置只认 spring.rabbitmq.*。所以你写的 username: admin/password:
admin123/virtual-host: andy 全部没生效，Spring 用的是内置默认值 guest/guest @ vhost /。broker 里没有 guest 用户 → ACCESS_REFUSED。

▎ 这也解释了为什么以前「能跑」：老配置是 guest/guest（顶级，同样被忽略），刚好撞上 Spring 默认值，那时候容器里有 guest 用户就连上了。现在容器只有 admin，guest
▎ 就被拒了。改密码没用，因为那几行压根没被读。

顺带一个潜藏 bug：acknowledge-mode: manual 也在顶级被忽略了，实际是 AUTO 模式，而你代码里手动 basicAck/basicNack——这迟早出问题。一并修正。

修之前先查一下 andy vhost 里是否已存在旧队列（参数不一致会导致修完 auth 后又报 inequivalent arg）：
