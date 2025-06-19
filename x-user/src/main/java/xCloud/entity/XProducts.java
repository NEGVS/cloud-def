package xCloud.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品表
 * @TableName x_products
 */
@TableName(value ="x_products")
@Data
public class XProducts implements Serializable {
    /**
     * 商品ID
     */
    @TableId
    private Long product_id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格：售卖价=优惠价
     */
    private BigDecimal price;

    /**
     * 商品价格：营销原价
     */
    private BigDecimal pre_price;

    /**
     * 商品价格：合作价格
     */
    private BigDecimal collaborate_price;

    /**
     * 商品价格：真实原价
     */
    private BigDecimal original_price;

    /**
     * 商品价格：成本价格
     */
    private BigDecimal cost_price;

    /**
     * 商品库存数量，-1：无限
     */
    private Integer stock;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 备注
     */
    private String notes;

    /**
     * 商品种类ID
     */
    private Integer category_id;

    /**
     * 商家ID
     */
    private Integer merchant_id;

    /**
     * 乐观锁版本号
     */
    private Long version;

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