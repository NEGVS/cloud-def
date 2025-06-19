package xCloud.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName(value ="x_merchants")
@Schema(name = "Merchants", description = "Merchants")
@Data
public class Merchants implements Serializable {
	@Serial
	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
//1-增加密码
//	2-删除userid
//	@TableId(type = IdType.AUTO)
	@Schema(name = "merchant_id",description = "商家ID")
	@TableField("merchant_id")
	private Integer merchant_id;

	@Schema(name = "name",description = "商家名称")
	@TableField("name")
	private String name;

	@Schema(name = "logo",description = "logo")
	@TableField("logo")
	private String logo;

	@Schema(name = "description",description = "description")
	@TableField("description")
	private String description;

	@Schema(name = "image",description = "商家图片")
	@TableField("image")
	private String image;

	@Schema(name = "address",description = "地址")
	@TableField("address")
	private String address;

	@Schema(name = "status",description = "状态")
	@TableField("status")
	private Integer status;

	@Schema(name = "sort",description = "排序")
	@TableField("sort")
	private Integer sort;

	@Schema(name = "packaging_rating",description = "包装评分")
	@TableField("packaging_rating")
	private BigDecimal packaging_rating;

	@Schema(name = "quantity_rating",description = "分量评分")
	@TableField("quantity_rating")
	private BigDecimal quantity_rating;

	@Schema(name = "taste_rating",description = "口味评分")
	@TableField("taste_rating")
	private BigDecimal taste_rating;

	@Schema(name = "phone",description = "电话")
	@TableField("phone")
	private String phone;

	@Schema(name = "email",description = "邮箱")
	@TableField("email")
	private String email;

	@Schema(name = "created_time",description = "创建时间")
	@TableField("created_time")
	private Date created_time;

	@Schema(name = "updated_time",description = "更新时间")
	@TableField("updated_time")
	private Date updated_time;


}