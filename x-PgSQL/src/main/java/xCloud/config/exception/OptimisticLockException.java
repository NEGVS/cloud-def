package xCloud.config.exception;

/**
 * @Description 测试建议：使用@EmbeddedKafka注解在JUnit中模拟Kafka，发送测试消息验证ack和补偿逻辑。
 *
 * 此实现确保高一致性：手动ack避免消息丢失，重试+补偿处理失败。如果需要OrderService的类似监听器或其他扩展，请补充细节！
 * @Author Andy Fan
 * @Date 2025/10/15 19:25
 * @ClassName OptimisticLockException
 */
public class OptimisticLockException extends RuntimeException {
    public OptimisticLockException(String message) {
        super(message);
    }
}