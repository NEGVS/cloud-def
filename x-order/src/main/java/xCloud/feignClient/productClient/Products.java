package xCloud.feignClient.productClient;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@TableName(value = "x_products")
@Schema(name = "products", description = "products")
@Data
public class Products implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(value = "product_id", type = IdType.AUTO)
    @Schema(name = "product_id", description = "商品ID")
    private Integer product_id;

    @Schema(name = "name", description = "商品名称")
    @TableField("name")
    private String name;

    @Schema(name = "description", description = "商品描述")
    @TableField("description")
    private String description;

    @Schema(name = "price", description = "商品价格：售卖价=优惠价")
    @TableField("price")
    private BigDecimal price;

    @Schema(name = "pre_price", description = "商品价格：营销原价")
    @TableField("pre_price")
    private BigDecimal pre_price;

    @Schema(name = "collaborate_price", description = "商品价格：合作价格")
    @TableField("collaborate_price")
    private BigDecimal collaborate_price;

    @Schema(name = "original_price", description = "商品价格：真实原价")
    @TableField("original_price")
    private BigDecimal original_price;

    @Schema(name = "cost_price", description = "商品价格：成本价格")
    @TableField("cost_price")
    private BigDecimal cost_price;

    @Schema(name = "stock", description = "商品库存数量，-1：无限")
    @TableField("stock")
    private Integer stock;

    @Schema(name = "image", description = "商品图片")
    @TableField("image")
    private String image;

    @Schema(name = "notes", description = "备注")
    @TableField("notes")
    private String notes;

    @Schema(name = "category_id", description = "商品种类ID")
    @TableField("category_id")
    private Integer category_id;

    @Schema(name = "merchant_id", description = "商家ID")
    @TableField("merchant_id")
    private Integer merchant_id;

    @Schema(name = "version", description = "乐观锁版本号")
    @TableField("version")
    private Integer version;

    @Schema(name = "created_time", description = "创建时间")
    @TableField("created_time")
    private Date created_time;

    @Schema(name = "updated_time", description = "更新时间")
    @TableField("updated_time")
    private Date updated_time;

}