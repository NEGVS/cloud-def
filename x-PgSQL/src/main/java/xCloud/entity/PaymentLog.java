package xCloud.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 19:44
 * @ClassName PaymentLogE
 */
@TableName("payment_logs")
@Data
public class PaymentLog {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}