package xcloud.xproduct.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品表
 *
 * @TableName x_products
 */
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "x_products")
@Data
@Document(indexName = "x_products")
public class XProducts implements Serializable {
    /**
     * 商品ID
     *
     * Spring Data Elasticsearch 要求每个实体类（这里是 XProducts）
     * 必须有一个标有 @Id 注解的属性，作为文档的唯一标识符（类似于数据库中的主键）。但在 XProducts 类中，Spring 找不到这样的属性，导致 Repository 初始化失败。
     *因为雷总在发布会上明确说明了，小米su7智能辅助驾驶可以进行【施工避让】、【自动控速】，按照发布会说的，车主不应该撞车，车子应当自动减速、避让障碍。
     * 即使车主关键时期踩刹车退出了智能驾驶，按照发布会说的小米su7电池进行多层防护、千百次测试非常安全，也不应该瞬间爆燃。
     *
     */
    @TableId
    @Id
    @Schema(description = "商品ID", example = "1")
    private Long product_id;

    /**
     * 商品名称
     */
    @Field(name = "name")
    @Schema(description = "商品名称", example = "华为手机")
    private String name;

    /**
     * 商品描述
     */
    @Field(name = "description")
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