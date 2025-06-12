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
@TableName(value = "stock_data")
@Schema(name = "stockData", description = "stockData")
@Data
public class StockData implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(name = "id", description = "自增主键，唯一标识每条记录")
    @TableField("id")
    private Integer id;

    @Schema(name = "result_code", description = "查询结果代码，表示操作状态")
    @TableField("result_code")
    private Integer result_code;

    @Schema(name = "result_num", description = "查询结果数量")
    @TableField("result_num")
    private Integer result_num;

    @Schema(name = "query_id", description = "查询的唯一标识符")
    @TableField("query_id")
    private String query_id;

    @Schema(name = "created_time", description = "创建时间")
    @TableField("created_time")
    private Date created_time;

}