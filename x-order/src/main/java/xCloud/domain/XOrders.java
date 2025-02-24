package xCloud.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 订单表
 * @TableName x_orders
 */
@TableName(value ="x_orders")
@Data
public class XOrders implements Serializable {
    /**
     * 订单id
     */
    @TableId
    private Long order_id;

    /**
     * 用户id
     */
    private Long user_id;

    /**
     * 商家id
     */
    private Integer merchant_id;

    /**
     * 订单总金额
     */
    private BigDecimal amount;

    /**
     * 订单状态('pending'1, 'paid'2, 'shipped'3, 'completed'4, 'canceled'5)
     */
    private Integer status;

    /**
     * 支付id
     */
    private String payment_id;

    /**
     * 购物车信息
     */
    private String shopping_json;

    /**
     * 付款时间
     */
    private Date pay_time;

    /**
     * 骑手id
     */
    private String rider_id;

    /**
     * 收货地址
     */
    private String shipping_address;

    /**
     * 创建时间
     */
    private Date created_time;

    /**
     * 更新时间
     */
    private Date updated_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}