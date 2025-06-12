package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "StockHeaderVO", description = "商保方案明细-参数集合对象")
public class StockHeaderVO extends PagerModel {
    @Schema(name = "id", description = "自增主键，唯一标识每条记录")
    private Integer id;

    @Schema(name = "can_sort", description = "是否可排序，1 表示可排序，0 表示不可排序")
    private Integer can_sort;

    @Schema(name = "key_name", description = "表头的键名，用于标识字段")
    private String key_name;

    @Schema(name = "name", description = "表头的显示名称")
    private String name;

    @Schema(name = "created_time", description = "创建时间")
    private Date created_time;


}
