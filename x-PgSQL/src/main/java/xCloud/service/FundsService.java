package xCloud.service;

import com.alibaba.fastjson.JSON;
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
import xCloud.mapper.LogsMapper;

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

    @Resource
    private LogsMapper logsMapper;

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
    @Transactional(rollbackFor = Exception.class)
    public void processPayment(ConsumerRecord<?, ?> record, Acknowledgment ack) {

        // 解析消息：假设value是Order的JSON
        Order order = (Order) record.value();  // JsonDeserializer自动转换
        String orderNo = order.getOrderNo();
        Long userId = order.getUserId();
        BigDecimal amount = order.getAmount();
        String key = "payment:" + order.getOrderNo();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            ack.acknowledge();
            return;  // 幂等
        }
        boolean deductionSuccess = false;  // 标记扣款是否成功
        BigDecimal actualDeducted = BigDecimal.ZERO;

        logsMapper.insertLogs("f1 监听：order-paid-topic 消费订单支付事件,进行资金扣款,订单号：" + orderNo, JSON.toJSONString(order));

        redisTemplate.opsForValue().set(key, "processed", Duration.ofHours(1));
        // 步骤1: 记录日志为PENDING
        fundsMapper.insertPaymentLog(orderNo, userId, amount, BigDecimal.ZERO, "PENDING", null);
        try {
            // 步骤2: 检查余额
            Funds funds = fundsMapper.selectOne(new QueryWrapper<Funds>().eq("user_id", order.getUserId()));
            if (funds == null || funds.getBalance().compareTo(order.getAmount()) < 0) {
                throw new InsufficientBalanceException("余额不足");
            }

            //步骤3:本地事务扣款（使用乐观锁）
            int rows = fundsMapper.deductBalance(order.getUserId(), order.getAmount(), funds.getVersion());
            //实际扣款
            if (rows == 0) {
                // 扣款失败（乐观锁）：FAILED，actual_deducted=0
                fundsMapper.updatePaymentLogStatus(orderNo, "FAILED", actualDeducted, "扣款并发冲突");
                log.warn("扣款失败，订单: {}", orderNo);
                ack.acknowledge();  // 确认
                return;
            }
            actualDeducted = amount;
            deductionSuccess = true;
            // 步骤4: 更新日志为SUCCESS
            fundsMapper.updatePaymentLogStatus(orderNo, "SUCCESS", actualDeducted, null);
            logsMapper.insertLogs("f2 扣款SUCCESS,订单号：" + orderNo, JSON.toJSONString(order));

            // 步骤5: 发布成功事件（用 Kafka 事务，确保原子）
            PaymentSuccessEvent successEvent = new PaymentSuccessEvent(order.getOrderNo(), order.getUserId(), order.getAmount());
            logsMapper.insertLogs("f3 发布成功事件：payment-success-topic,订单号：" + orderNo, JSON.toJSONString(successEvent));
            kafkaTemplate.executeInTransaction(t -> {
                kafkaTemplate.send("payment-success-topic", order.getOrderNo(), successEvent);
                return null;
            });

            // 步骤6: 手动确认 成功后提交偏移
            ack.acknowledge();
            logsMapper.insertLogs("f4 手动确认 已经消费成功：payment-success-topic,订单号：" + orderNo, JSON.toJSONString(successEvent));

            // 日志记录
            log.info("支付成功，订单号: {}, 扣款金额: {}", orderNo, amount);

        } catch (Exception e) {
            // 扣款后异常：更新 FAILED（actual_deducted=amount）
            fundsMapper.updatePaymentLogStatus(orderNo, "FAILED", actualDeducted, e.getMessage());
            // 发 failed event 触发补偿（Saga）
            kafkaTemplate.send("payment-failed-topic", orderNo, new PaymentFailedEvent(orderNo, userId, amount, e.getMessage()));
            logsMapper.insertLogs("f4 支付失败（扣款后），发布失败事件：payment-failed-topic", JSON.toJSONString(new PaymentFailedEvent(orderNo, userId, amount, e.getMessage())));
            log.error("支付失败（扣款后），订单: {}, 错误: {}", orderNo, e.getMessage());

            // 优化：仅扣款成功后才处理补偿
//            if (deductionSuccess) {
//                // 扣款后异常：更新 FAILED（actual_deducted=amount）
//                fundsMapper.updatePaymentLogStatus(orderNo, "FAILED", actualDeducted, e.getMessage());
//                // 发 failed event 触发补偿（Saga）
//                kafkaTemplate.send("payment-failed-topic", orderNo, new PaymentFailedEvent(orderNo, userId, amount, e.getMessage()));
//                logsMapper.insertLogs("f4 支付失败（扣款后），发布失败事件：payment-failed-topic", JSON.toJSONString(new PaymentFailedEvent(orderNo, userId, amount, e.getMessage())));
//                log.error("支付失败（扣款后），订单: {}, 错误: {}", orderNo, e.getMessage());
//                // 不 throw：已发事件，补偿处理；避免事务回滚（因为补偿异步）
//                // ack.acknowledge();  // 可选：如果不想重试整个消息
//            } else {
//                // 扣款前异常：已早返回或日志 FAILED，无需补偿
//                log.error("支付前异常，订单: {}, 错误: {}", orderNo, e.getMessage());
//                ack.acknowledge();  // 确认结束
//            }
            // 无 throw e：避免不必要的重试；ErrorHandler 处理剩余异常
        }
    }


    /**
     * 额外：Saga补偿监听器（消费失败事件，回滚资金）
     *
     * @param event  event
     * @param record record
     * @param ack    ack
     *               优化说明
     *               手动确认位置：
     *               成功路径：在回滚余额、更新日志后调用 ack.acknowledge()，确保补偿原子后提交（结合 @Transactional，如果事务回滚，会重新抛异常不 ack）。
     *               幂等/无需补偿：也 ack，避免消息卡在 Lag 中。
     *               异常路径：不 ack，Kafka 会根据配置（retries=3）重试；超过后 errorHandler 发送 DLQ。
     *               <p>
     *               幂等性：用 Redis 缓存订单号，防止重复回滚（资金账户有乐观锁辅助）。
     *               日志增强：添加偏移（record.offset()）和结构化 JSON，便于追踪（e.g., Offset Explorer 中搜索偏移）。
     *               性能影响：手动 ack 增加少量延迟，但提升一致性（Lag 清零更快）。测试中，Lag 应在 1-3s 内从 5 → 0。
     *               测试建议：
     *               模拟失败：临时抛异常，观察 Offset Explorer Lag 增加，重试后清零。
     *               监控：日志搜索 "偏移"，确认 ack 执行。
     */
    @KafkaListener(topics = "payment-failed-topic", groupId = "fund-group", errorHandler = "customKafkaErrorHandler")
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentFailed(PaymentFailedEvent event, ConsumerRecord<?, ?> record, Acknowledgment ack) {
        logsMapper.insertLogs("failed 1 监听 消费失败事件：payment-failed-topic, 偏移: " + record.offset(), JSON.toJSONString(event));

        // 步骤0: 幂等检查（使用 Redis，防止重复补偿）
        String key = "compensation:" + event.getOrderNo();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            log.info("补偿幂等，已处理，订单号: {}, 偏移: {}", event.getOrderNo(), record.offset());
            ack.acknowledge();  // 幂等时确认，避免卡住
            return;
        }
        redisTemplate.opsForValue().set(key, "compensated", Duration.ofHours(24));  // 过期 24h
        // 步骤1: 记录初始日志
        logsMapper.insertLogs("failed 1 消费失败事件：payment-failed-topic, 偏移: " + record.offset(), JSON.toJSONString(event));

        String orderNo = event.getOrderNo();
        Long userId = event.getUserId();
        try {
            // 步骤2: 从日志获取已扣金额（如果日志中是FAILED，则返回amount，否则0）
            BigDecimal deductedAmount = fundsMapper.getDeductedAmountFromLog(orderNo);
            if (deductedAmount == null || deductedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("无扣款记录，无需补偿，订单号: {}", orderNo);
                logsMapper.insertLogs("failed 2 无扣款记录，无需补偿，订单号", JSON.toJSONString(orderNo));
                ack.acknowledge();  // 无需补偿也确认
                return;
            }
            // 步骤3: 查询资金账户
            Funds funds = fundsMapper.selectOne(new QueryWrapper<Funds>().eq("user_id", event.getUserId()));
            if (funds == null) {
                throw new RuntimeException("用户资金账户不存在");
            }
            // 步骤4: 回滚余额（乐观锁）
            int rows = fundsMapper.updateBalance(userId, deductedAmount, funds.getVersion());
            if (rows == 0) {
                throw new OptimisticLockException("回滚并发冲突");
            }
            logsMapper.insertLogs("failed 3 回滚余额" + deductedAmount, JSON.toJSONString(userId));

            // 步骤5: 更新日志为COMPENSATED
            fundsMapper.updatePaymentLogStatus(orderNo, "COMPENSATED", deductedAmount, "补偿完成");

            // 步骤6: 记录成功日志
            logsMapper.insertLogs("failed 3 回滚余额: " + deductedAmount + ", 用户ID: " + userId, JSON.toJSONString(event));
            log.info("支付补偿成功，订单号: {}, 回滚金额: {}, 偏移: {}", orderNo, deductedAmount, record.offset());

            // 步骤7: 手动确认（补偿成功后提交偏移）
            ack.acknowledge();
        } catch (Exception e) {
            // 步骤8: 异常日志（不 ack，触发重试）
            log.error("补偿处理异常，订单号: {}, 偏移: {}, 错误: {}", orderNo, record.offset(), e.getMessage(), e);
            logsMapper.insertLogs("failed 4 补偿异常: " + e.getMessage(), JSON.toJSONString(event));
            // errorHandler 会进一步处理（如 DLQ）
            throw e;  // 抛出让 Kafka/ errorHandler 重试
        }
    }
}
