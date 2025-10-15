package xCloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xCloud.config.exception.InsufficientBalanceException;
import xCloud.config.exception.OptimisticLockException;
import xCloud.entity.Funds;
import xCloud.entity.Order;
import xCloud.entity.PaymentFailedEvent;
import xCloud.entity.PaymentSuccessEvent;
import xCloud.mapper.FundsMapper;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 19:15
 * @ClassName FundsService
 */
@Slf4j
@Service
public class FundsService {
    @Resource
    private FundsMapper fundsMapper;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Resource
    private RedisTemplate<String, String> redisTemplate;  // 幂等检查

    /**
     * 消费订单支付事件，进行资金扣款
     * 配置：需在application.yml中添加
     * spring:
     * kafka:
     * consumer:
     * value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
     * properties:
     * spring.json.trusted.packages: com.yourpackage.entity  # 允许反序列化
     */
    @KafkaListener(topics = "order-paid-topic", groupId = "fund-group")
    public void processPayment(ConsumerRecord<?, ?> record, Acknowledgment ack) {
        // 解析消息：假设value是Order的JSON
        Order order = (Order) record.value();  // JsonDeserializer自动转换
        String orderNo = order.getOrderNo();
        Long userId = order.getUserId();
        BigDecimal amount = order.getAmount();
        String key = "payment:" + order.getOrderNo();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return;  // 幂等
        }
        redisTemplate.opsForValue().set(key, "processed", Duration.ofHours(1));
        // 步骤1: 记录日志为PENDING
        fundsMapper.insertPaymentLog(orderNo, userId, amount, "PENDING", null);
        try {
            // 步骤2: 检查余额
            Funds funds = fundsMapper.selectOne(new QueryWrapper<Funds>().eq("user_id", order.getUserId()));
            if (funds == null || funds.getBalance().compareTo(order.getAmount()) < 0) {
                throw new InsufficientBalanceException("余额不足");
            }

            //步骤3:本地事务扣款（使用乐观锁）
            int rows = fundsMapper.deductBalance(order.getUserId(), order.getAmount(), funds.getVersion());
            if (rows == 0) {
                throw new OptimisticLockException("并发冲突");
            }
            // 步骤4: 更新日志为SUCCESS
            fundsMapper.updatePaymentLogStatus(orderNo, "SUCCESS", null);

            // 步骤5: 发布成功事件
            PaymentSuccessEvent successEvent = new PaymentSuccessEvent(order.getOrderNo(), order.getUserId(), order.getAmount());
            kafkaTemplate.send("payment-success-topic", order.getOrderNo(), successEvent);
            // 步骤6: 手动确认 成功后提交偏移
            ack.acknowledge();
            // 日志记录
            log.info("支付成功，订单号: {}, 扣款金额: {}", orderNo, amount);

        } catch (Exception e) {
            // 更新日志为FAILED
            fundsMapper.updatePaymentLogStatus(orderNo, "FAILED", e.getMessage());

            // 不确认ack，Kafka会重试（配置max.poll.records=1避免批量失败）
            log.error("支付失败，订单号: {}, 错误: {}", orderNo, e.getMessage(), e);
            // Saga补偿：发布失败事件，订单服务监听回滚
            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                    orderNo, order.getUserId(), amount, e.getMessage());
            kafkaTemplate.send("payment-failed-topic", orderNo, failedEvent);
            // 可选：超过重试阈值后，发送到死信队列（需配置ErrorHandler）
            throw e;  // 抛出让Kafka重试
        }
    }

    // 额外：Saga补偿监听器（消费失败事件，回滚资金）
    @KafkaListener(topics = "payment-failed-topic", groupId = "fund-group")
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        String orderNo = event.getOrderNo();
        Long userId = event.getUserId();
        try {
            // 从日志获取已扣金额（如果日志中是FAILED，则返回amount，否则0）
            BigDecimal deductedAmount = fundsMapper.getDeductedAmountFromLog(orderNo);
            if (deductedAmount == null || deductedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("无扣款记录，无需补偿，订单号: {}", orderNo);
                return;
            }
            // 查询资金账户
            Funds funds = fundsMapper.selectOne(new QueryWrapper<Funds>().eq("user_id", event.getUserId()));
            if (funds == null) {
                throw new RuntimeException("用户资金账户不存在");
            }
            // 回滚余额（乐观锁）
            int rows = fundsMapper.updateBalance(userId, deductedAmount, funds.getVersion());
            if (rows == 0) {
                throw new OptimisticLockException("回滚并发冲突");
            }
            // 更新日志为COMPENSATED
            fundsMapper.updatePaymentLogStatus(orderNo, "COMPENSATED", "补偿完成");

            log.info("支付补偿成功，订单号: {}, 回滚金额: {}", orderNo, deductedAmount);
        } catch (Exception e) {
            log.error("补偿处理异常，订单号: {}, 错误: {}", event.getOrderNo(), e.getMessage(), e);
            // 可进一步人工干预或死信队列
        }
    }
}
