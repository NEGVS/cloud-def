package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper=false)
@Schema(name = "MerchantsDTO", description = "商保方案明细-参数集合对象")
public class MerchantsVO extends PagerModel {

    @Schema(name = "merchant_id", description = "商家ID")
    private Integer merchant_id;

    @Schema(name = "user_id", description = "商家用户ID")
    private String user_id;

    @Schema(name = "name", description = "商家名称")
    private String name;

    @Schema(name = "logo", description = "logo")
    private String logo;

    @Schema(name = "description", description = "description")
    private String description;

    @Schema(name = "image", description = "商家图片")
    private String image;

    @Schema(name = "address", description = "地址")
    private String address;

    @Schema(name = "status", description = "状态")
    private Integer status;

    @Schema(name = "sort", description = "排序")
    private Integer sort;

    @Schema(name = "packaging_rating", description = "包装评分")
    private BigDecimal packaging_rating;

    @Schema(name = "quantity_rating", description = "分量评分")
    private BigDecimal quantity_rating;

    @Schema(name = "taste_rating", description = "口味评分")
    private BigDecimal taste_rating;

    @Schema(name = "phone", description = "电话")
    private String phone;

    @Schema(name = "email", description = "邮箱")
    private String email;


    /**
     * 商品-------------------
     */

    @Schema(name = "product_id", description = "商品ID")
    private Integer product_id;

    @Schema(name = "name", description = "商品名称")
    private String product_name;

    @Schema(name = "description", description = "商品描述")
    private String product_description;

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

    @Schema(name = "product_image", description = "商品图片")
    private String product_image;

    @Schema(name = "notes", description = "备注")
    private String notes;

    @Schema(name = "category_id", description = "商品种类ID")
    private Integer category_id;


    @Schema(name = "version", description = "乐观锁版本号")
    private Integer version;

}
