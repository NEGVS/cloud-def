package xCloud.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品表
 *
 * @TableName x_products
 */
@Schema(description = "XProductsB")
@TableName(value = "x_products")
@Data
public class XProductsB implements Serializable {
    /**
     * 商品ID
     */
    @TableId
    @Schema(description = "product_id")
    @TableField(value = "product_id")
    private Long product_id;

    /**
     * 商品名称
     */
    @Schema(description = "name")
    @TableField(value = "name")
    private String name;

    /**
     * 商品描述
     */
    @Schema(description = "description")
    @TableField(value = "description")
    private String description;

    /**
     * 商品价格：售卖价=优惠价
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 商品价格：营销原价
     */
    @TableField(value = "pre_price")
    private BigDecimal pre_price;

    /**
     * 商品价格：合作价格
     */
    @TableField(value = "collaborate_price")
    private BigDecimal collaborate_price;

    /**
     * 商品价格：真实原价
     */
    @TableField(value = "original_price")
    private BigDecimal original_price;

    /**
     * 商品价格：成本价格
     */
    @TableField(value = "cost_price")
    private BigDecimal cost_price;

    /**
     * 商品库存数量，-1：无限
     */
    @TableField(value = "stock")
    private Integer stock;

    /**
     * 商品图片
     */
    @TableField(value = "image")
    private String image;

    /**
     * 备注
     */
    @TableField(value = "notes")
    private String notes;

    /**
     * 商品种类ID
     */
    @TableField(value = "category_id")
    private Integer category_id;

    /**
     * 商家ID
     */
    @TableField(value = "merchant_id")
    private Integer merchant_id;

    /**
     * 乐观锁版本号
     */
    @TableField(value = "version")
    private Long version;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date created_time;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private Date updated_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}