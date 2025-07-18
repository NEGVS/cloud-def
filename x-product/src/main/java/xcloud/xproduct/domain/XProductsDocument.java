package xcloud.xproduct.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description elasticsearch use，定义商品实体和索引ProductDocument
 * @Author Andy Fan
 * @Date 2025/3/4 15:46
 * @ClassName XProductsDocument
 * 你需要在实体类 XProducts 或 XProductsDocument 上显式指定小写的索引名称。可以通过 @Document 注解来设置索引名称。
 */
@Data
@Document(indexName = "x_products")
public class XProductsDocument {
    /**
     * 商品ID
     */

    @Id
    private Long product_id;
    /**
     * 商品名称
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String name;
    /**
     * 商品描述
     */
    @Field(type = FieldType.Keyword)
    private String description;
    /**
     * 商品价格：售卖价=优惠价
     */
    @Field(type = FieldType.Double)
    private BigDecimal price;
    /**
     * 商品价格：营销原价
     */
    @Field(type = FieldType.Double)
    private BigDecimal pre_price;
    /**
     * 商品价格：合作价格
     */
    @Field(type = FieldType.Double)
    private BigDecimal collaborate_price;
    /**
     * 商品价格：真实原价
     */
    @Field(type = FieldType.Double)
    private BigDecimal original_price;
    /**
     * 商品价格：成本价格
     */
    @Field(type = FieldType.Double)
    private BigDecimal cost_price;
    /**
     * 商品库存数量，-1：无限
     */
    @Field(type = FieldType.Integer)
    private Integer stock;
    /**
     * 商品图片
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String image;
    /**
     * 备注
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String notes;
    /**
     * 商品种类ID
     */
    @Field(type = FieldType.Integer)
    private Integer category_id;
    /**
     * 商家ID
     */
    @Field(type = FieldType.Integer)
    private Integer merchant_id;
    /**
     * 乐观锁版本号
     */
    @Field(type = FieldType.Long)
    private Long version;
    /**
     * 创建时间
     */
    @Field(type = FieldType.Date)
    private Date created_time;
    /**
     * 更新时间
     */
    @Field(type = FieldType.Date)
    private Date updated_time;


}
