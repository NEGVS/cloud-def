package xcloud.xproduct.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xCloud.entity.PagerModel;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
@Schema(name = "ProductsDTO", description = "商保方案明细-参数集合对象")
public class ProductsVO extends PagerModel {
    @Schema(name = "product_id", description = "商品ID")
    private Integer product_id;

    @Schema(name = "name", description = "商品名称")
    private String name;

    @Schema(name = "description", description = "商品描述")
    private String description;

    @Schema(name = "price", description = "商品价格：售卖价=优惠价")
    private BigDecimal price;

    @Schema(name = "pre_price", description = "商品价格：营销原价")
    private BigDecimal pre_price;

    @Schema(name = "collaborate_price", description = "商品价格：合作价格")
    private BigDecimal collaborate_price;

    @Schema(name = "original_price", description = "商品价格：真实原价")
    private BigDecimal original_price;

    @Schema(name = "cost_price", description = "商品价格：成本价格")
    private BigDecimal cost_price;

    @Schema(name = "stock", description = "商品库存数量，-1：无限")
    private Integer stock;

    @Schema(name = "image", description = "商品图片")
    private String image;

    @Schema(name = "notes", description = "备注")
    private String notes;

    @Schema(name = "category_id", description = "商品种类ID")
    private Integer category_id;

    @Schema(name = "merchant_id", description = "商家ID")
    private Integer merchant_id;

    @Schema(name = "version", description = "乐观锁版本号")
    private Integer version;

    @Schema(name = "created_time", description = "创建时间")
    private Date created_time;

    @Schema(name = "updated_time", description = "更新时间")
    private Date updated_time;


}
