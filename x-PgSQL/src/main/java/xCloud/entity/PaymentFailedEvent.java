package xCloud.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 19:20
 * @ClassName PaymentFailedEvent
 */
@Data
public class PaymentFailedEvent implements Serializable {
    private String orderNo;
    private Long userId;// 新增：用户ID，用于补偿查询
    private BigDecimal amount;
    private String errorMessage;
    private String timestamp;

    public PaymentFailedEvent(String orderNo, Long userId, BigDecimal amount,String errorMessage) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.amount = amount;
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now().toString();
    }
}