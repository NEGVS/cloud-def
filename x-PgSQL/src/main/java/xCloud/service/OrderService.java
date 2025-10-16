package xCloud.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xCloud.entity.Order;
import xCloud.entity.PaymentSuccessEvent;
import xCloud.entity.request.CreateOrderRequest;
import xCloud.mapper.LogsMapper;
import xCloud.mapper.OrderMapper;
import xCloud.mapper.OutboxMapper;

import java.util.UUID;

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
    private OrderMapper orderMapper;
    @Resource
    private LogsMapper logsMapper;
    @Resource
    private OutboxMapper outboxMapper;
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

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

}
