package xCloud.entity.merchants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xCloud.entity.PagerModel;

import java.math.BigDecimal;
import java.util.Date;
@Data
@Schema(name = "MerchantsDTO", description = "商保方案明细-参数集合对象")
public class MerchantsVO  extends PagerModel {	@Schema(name = "merchant_id",description = "商家ID")
	private Integer merchant_id;

	@Schema(name = "user_id",description = "商家用户ID")
	private String user_id;

	@Schema(name = "name",description = "商家名称")
	private String name;

	@Schema(name = "logo",description = "logo")
	private String logo;

	@Schema(name = "description",description = "description")
	private String description;

	@Schema(name = "image",description = "商家图片")
	private String image;

	@Schema(name = "address",description = "地址")
	private String address;

	@Schema(name = "status",description = "状态")
	private Integer status;

	@Schema(name = "sort",description = "排序")
	private Integer sort;

	@Schema(name = "packaging_rating",description = "包装评分")
	private BigDecimal packaging_rating;

	@Schema(name = "quantity_rating",description = "分量评分")
	private BigDecimal quantity_rating;

	@Schema(name = "taste_rating",description = "口味评分")
	private BigDecimal taste_rating;

	@Schema(name = "phone",description = "电话")
	private String phone;

	@Schema(name = "email",description = "邮箱")
	private String email;

	@Schema(name = "created_time",description = "创建时间")
	private Date created_time;

	@Schema(name = "updated_time",description = "更新时间")
	private Date updated_time;

	@Schema(name = "password",description = "密码")
	private String password;

	@Schema(name = "open_id",description = "openID")
	private String open_id;


}
