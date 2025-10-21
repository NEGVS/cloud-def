package xCloud.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xCloud.entity.Order;
import xCloud.entity.PaymentSuccessEvent;
import xCloud.entity.request.CreateOrderRequest;
import xCloud.mapper.LogsMapper;
import xCloud.mapper.OrderMapper;
import xCloud.mapper.OutboxMapper;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 17:29
 * @ClassName OrderService
 */

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderService {

    @Resource
    private StringRedisTemplate redisTemplate;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private LogsMapper logsMapper;

    @Resource
    private OutboxMapper outboxMapper;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    // 预设幂等 key，防止重复消费
    private static final String IDEMPOTENT_PREFIX = "order:processed:";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderService(OrderMapper orderMapper, OutboxMapper outboxMapper) {
        this.orderMapper = orderMapper;
        this.outboxMapper = outboxMapper;
    }


    @Transactional
    public String createOrder(CreateOrderRequest request) {
        // 生成订单号
        String orderNo = UUID.randomUUID().toString().replace("-", "");

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setAmount(request.getAmount());
        order.setStatus("PENDING");
        orderMapper.insert(order);
        logsMapper.insertLogs("0 订单创建成功", "订单号：" + orderNo);
        // 发布Kafka事件，开启事务
        kafkaTemplate.executeInTransaction(t -> {
            try {
                String orderJson = objectMapper.writeValueAsString(order);
                t.send("order-paid-topic", orderNo, order);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Kafka 消息序列化失败", e);
            }
            return null;
        });
        logsMapper.insertLogs("1 发布Kafka事件:order-paid-topic,订单号：" + orderNo, JSON.toJSONString(order));
        return orderNo;
    }


    /**
     * 消费支付确认事件，更新订单状态
     * 添加异常处理与手动 ack：
     * 更新 handlePaymentSuccess 为以下版本（添加 try-catch、Acknowledgment 和日志异常记录）。这确保即使失败，也可配置重试或 DLQ，避免无限 Lag。
     *
     * @param event
     */
    @KafkaListener(topics = "payment-success-topic", groupId = "order-group", errorHandler = "customKafkaErrorHandler")
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentSuccess(PaymentSuccessEvent event, ConsumerRecord<?, ?> record, Acknowledgment ack) {
        try {
            Order order = orderMapper.selectByOrderNo(event.getOrderNo());
            if (order != null && "PENDING".equals(order.getStatus())) {
                logsMapper.insertLogs("2 监听：payment-success-topic 消费支付确认事件，更新订单状态:PAID,订单号：" + event.getOrderNo() + ", 偏移: " + record.offset(), JSON.toJSONString(event));
                order.setStatus("PAID");
                orderMapper.updateById(order);  // 乐观锁自动处理
                ack.acknowledge();  // 手动确认成功
                log.info("订单更新成功: {}", event.getOrderNo() + ", 偏移: " + record.offset());
            } else {
                ack.acknowledge();  // 非 PENDING 也确认，避免卡住
            }
        } catch (Exception e) {
            log.error("消费失败，订单号: {}, 错误: {},偏移:{}", event.getOrderNo(), e.getMessage(), record.offset(), e);
            // 可发送到 DLQ 或重试；不 ack 触发 Kafka 重试
            throw e;  // 或自定义 ErrorHandler
        }
    }
    //--------------------------1020-1--------------------------

    /**
     * Kafka 消费端异步处理订单（幂等设计）
     */
    @KafkaListener(topics = "order-topic", groupId = "order-group")
    public void consumeOrder(String message) {
        Map<String, Object> orderMsg = JSON.parseObject(message, Map.class);
        Long orderId = (long) orderMsg.get("orderId");
        String productId = (String) orderMsg.get("productId");
        int num = (int) orderMsg.get("num");
        String idempotentKey = IDEMPOTENT_PREFIX + orderId;
        try {
            // 幂等检查
            Boolean processed = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 1, TimeUnit.DAYS);
            if (processed == null || !processed) {
                log.warn("重复消费消息, orderId={}", orderId);
                return;
            }
            // 模拟限流，可用 RateLimiter 或 Kafka 消费端限流策略
            // 处理订单逻辑
            Order order = new Order();
            order.setId(orderId);
            int insert = orderMapper.insert(order);
            if (insert > 0) {
                log.info("订单创建成功, orderId={}, productId={}, num={}", orderId, productId, num);
            } else {
                log.info("订单创建失败, orderId={}, productId={}, num={}", orderId, productId, num);
            }
        } catch (Exception e) {
            // 订单创建失败，回滚 Redis 库存
            redisTemplate.opsForValue().decrement("stock:" + productId, num);
            log.error("订单消费失败, orderId={}, rollback stock, exception={}", orderId, e.getMessage());
            // 可发送告警邮件或消息
        }

    }

    /**
     * 定时任务同步库存到数据库
     */
    //    @Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(fixedRate = 1000 * 6)
    public void syncStockToDB() {
        Set<String> keys = redisTemplate.keys("stock:*");
        if (keys == null) return;
        for (String key : keys) {
            String productId = key.split(":")[1];
            int redisStock = Integer.parseInt(redisTemplate.opsForValue().get(key));
            int update = orderMapper.update(new Order(), new QueryWrapper<Order>().eq("product_id", productId));
            if (update > 0) {
                log.info("同步库存成功, productId={}, redisStock={}", productId, redisStock);
            } else {
                log.info("同步库存失败, productId={}, redisStock={}", productId, redisStock);
            }
        }
    }
}
