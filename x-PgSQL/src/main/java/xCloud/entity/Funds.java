package xCloud.entity;




import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 17:23
 * @ClassName Funds
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("funds")
public class Funds extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal balance;
}