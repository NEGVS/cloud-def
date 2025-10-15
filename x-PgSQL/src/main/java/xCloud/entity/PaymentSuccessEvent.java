package xCloud.entity;

/**
 * @Description 基于您提到的手动确认机制（使用ConsumerRecord<?, ?> record和Acknowledgment ack），我将为资金服务（FundsService）中的支付处理监听器提供完整实现。同时，补充缺失的PaymentSuccessEvent事件类定义。该监听器消费“order-paid-topic”主题的消息，处理订单支付扣款逻辑，并手动提交偏移量以确保exactly-once语义。
 * 1. 事件类定义（PaymentSuccessEvent & PaymentFailedEvent）
 * @Author Andy Fan
 * @Date 2025/10/15 19:19
 * @ClassName PaymentSuccessEvent
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent implements Serializable {
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String timestamp;  // ISO格式时间戳

    public PaymentSuccessEvent(String orderNo) {
        this.orderNo = orderNo;
        this.timestamp = LocalDateTime.now().toString();
    }

    public PaymentSuccessEvent(String orderNo, Long userId, BigDecimal amount) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now().toString();
    }
}

