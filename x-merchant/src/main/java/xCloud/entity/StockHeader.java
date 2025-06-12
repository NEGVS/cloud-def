package xCloud.entity;

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
import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@TableName(value ="stock_header")
@Schema(name = "stockHeader", description = "stockHeader")
@Data
public class StockHeader implements Serializable {
	@Serial
	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	@Schema(name = "id",description = "自增主键，唯一标识每条记录")
	@TableField("id")
	private Integer id;


	@Schema(name = "can_sort",description = "是否可排序，1 表示可排序，0 表示不可排序")
	@TableField("can_sort")
	private Integer can_sort;

	@Schema(name = "key_name",description = "表头的键名，用于标识字段")
	@TableField("key_name")
	private String key_name;

	@Schema(name = "name",description = "表头的显示名称")
	@TableField("name")
	private String name;

	@Schema(name = "created_time",description = "创建时间")
	@TableField("created_time")
	private Date created_time;

}