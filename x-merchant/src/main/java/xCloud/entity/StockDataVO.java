package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xCloud.entity.PagerModel;

import java.util.Date;

@Data
@Schema(name = "StockDataVO", description = "商保方案明细-参数集合对象")
public class StockDataVO extends PagerModel {
    @Schema(name = "id", description = "自增主键，唯一标识每条记录")
    private Integer id;

    @Schema(name = "result_code", description = "查询结果代码，表示操作状态")
    private Integer result_code;

    @Schema(name = "result_num", description = "查询结果数量")
    private Integer result_num;

    @Schema(name = "query_id", description = "查询的唯一标识符")
    private String query_id;

    @Schema(name = "created_time", description = "创建时间")
    private Date created_time;


}
