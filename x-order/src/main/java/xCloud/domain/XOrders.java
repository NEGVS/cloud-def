package xCloud.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单表
 *
 * @TableName x_orders
 */
@TableName(value = "x_orders")
@Data
@Schema(description = "XOrders entity")
public class XOrders implements Serializable {
    /**
     * 订单id
     */
    @TableId
    @Schema(description = "订单id", example = "1")
    private Long order_id;

    /**
     * 用户id
     */
    @Schema(description = "用户id", example = "1")
    private Long user_id;

    /**
     * 商家id
     */
    @Schema(description = "商家id", example = "1")
    private Integer merchant_id;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额", example = "1000")
    private BigDecimal amount;

    /**
     * 订单状态('pending'1, 'paid'2, 'shipped'3, 'completed'4, 'canceled'5)
     */
    @Schema(description = "订单状态('pending'1, 'paid'2, 'shipped'3, 'completed'4, 'canceled'5)", example = "1")
    private Integer status;

    /**
     * 支付id
     */
    @Schema(description = "支付id", example = "1")
    private String payment_id;

    /**
     * 购物车信息
     */
    @Schema(description = "购物车信息", example = "1")
    private String shopping_json;

    /**
     * 付款时间
     */
    @Schema(description = "付款时间", example = "")
    private Date pay_time;

    /**
     * 骑手id
     */

    private String rider_id;

    /**
     * 收货地址
     */
    private String shipping_address;

    private Integer a;

    private Integer b;

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