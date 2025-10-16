package xCloud.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 21:48
 * @ClassName Logs
 */
@TableName("logs")
@Data
public class Logs {
    private Long id;
    private String remark;
    private String note;
}
