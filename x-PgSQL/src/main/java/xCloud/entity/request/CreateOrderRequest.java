package xCloud.entity.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 17:27
 * @ClassName CreateOrderRequest
 */
@Data
public class CreateOrderRequest {
    private BigDecimal amount;
    private Long userId;
    private Long amountFen;
    private String idempotencyKey;
}
