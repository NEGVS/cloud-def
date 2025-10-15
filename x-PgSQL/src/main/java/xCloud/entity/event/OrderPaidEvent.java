package xCloud.entity.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 20:58
 * @ClassName OrderPaidEvent
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaidEvent {
    private String orderNo;
    private BigDecimal amount;
    private String status;
}