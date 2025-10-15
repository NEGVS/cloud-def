package xCloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xCloud.entity.Order;
import xCloud.entity.PaymentSuccessEvent;
import xCloud.entity.request.CreateOrderRequest;
import xCloud.mapper.OrderMapper;
import xCloud.mapper.OutboxMapper;

import java.util.UUID;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 17:29
 * @ClassName OrderService
 */


@Service
@Transactional(rollbackFor = Exception.class)
public class OrderService {


    @Resource
    private final OrderMapper orderMapper;
    @Resource
    private final OutboxMapper outboxMapper;
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
        // 发布Kafka事件，开启事务
        kafkaTemplate.executeInTransaction(t -> {
            kafkaTemplate.send("order-paid-topic", orderNo, order);
            return null;
        });
        return orderNo;
    }

    // 消费支付确认事件，更新订单状态
    @KafkaListener(topics = "payment-success-topic", groupId = "order-group")
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        Order order = orderMapper.selectByOrderNo(event.getOrderNo());
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("PAID");
            orderMapper.updateById(order);  // 乐观锁自动处理
        }
    }

}
