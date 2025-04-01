package xcloud.xproduct.domain;

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
@TableName(value = "x_products")
@Data
public class XProducts implements Serializable {
    /**
     * 商品ID
     */
    @TableId
    @Schema(description = "商品ID", example = "1")
    private Long product_id;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "华为手机")
    private String name;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述", example = "华为手机")
    private String description;

    /**
     * 商品价格：售卖价=优惠价
     */
    @Schema(description = "商品价格：售卖价=优惠价", example = "1000")
    private BigDecimal price;

    /**
     * 商品价格：营销原价
     */
    @Schema(description = "商品价格：营销原价", example = "1000")
    private BigDecimal pre_price;

    /**
     * 商品价格：合作价格
     */
    @Schema(description = "商品价格：合作价格", example = "1000")
    private BigDecimal collaborate_price;

    /**
     * 商品价格：真实原价
     */
    @Schema(description = "商品价格：真实原价", example = "1000")
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
    @Schema(description = "商家ID", example = "1")
    private Integer merchant_id;

    /**
     * 乐观锁版本号
     */
    @Schema(description = "乐观锁版本号", example = "1")
    private Long version;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-05-01 00:00:00")
    private Date created_time;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2023-05-01 00:00:00")
    private Date updated_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}