package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "StockVO", description = "商保方案明细-参数集合对象")
public class StockVO extends PagerModel {

    @Schema(name = "id", description = "自增主键，唯一标识每条记录")
    private Integer id;

    @Schema(name = "block_name", description = "股票所属板块名称")
    private String block_name;

    @Schema(name = "code", description = "股票代码，唯一标识股票")
    private String code;

    @Schema(name = "exchange", description = "股票交易所名称")
    private String exchange;

    @Schema(name = "finance_type", description = "财务类型")
    private String finance_type;

    @Schema(name = "heat", description = "股票热度指标")
    private String heat;

    @Schema(name = "last_px", description = "最新价格")
    private String last_px;

    @Schema(name = "market", description = "股票市场")
    private String market;

    @Schema(name = "name", description = "股票名称")
    private String name;

    @Schema(name = "logo_type", description = "图标类型")
    private String logo_type;

    @Schema(name = "logo_url", description = "图标的 URL 或路径")
    private String logo_url;

    @Schema(name = "px_change_rate", description = "价格变化率（如百分比）")
    private String px_change_rate;

    @Schema(name = "rank_diff", description = "排名变化")
    private String rank_diff;

    @Schema(name = "created_time", description = "创建时间")
    private Date created_time;


}
