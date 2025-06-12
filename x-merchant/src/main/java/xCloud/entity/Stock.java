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
@TableName(value = "stock")
@Schema(name = "stock", description = "stock")
@Data
public class Stock extends PagerModel implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = -8643680747004267055L;


    @TableId(type = IdType.AUTO)
    @Schema(name = "id", description = "自增主键，唯一标识每条记录")
    @TableField("id")
    private Integer id;

    @Schema(name = "block_name", description = "股票所属板块名称")
    @TableField("block_name")
    private String block_name;

    @Schema(name = "code", description = "股票代码，唯一标识股票")
    @TableField("code")
    private String code;

    @Schema(name = "exchange", description = "股票交易所名称")
    @TableField("exchange")
    private String exchange;

    @Schema(name = "finance_type", description = "财务类型")
    @TableField("finance_type")
    private String finance_type;

    @Schema(name = "heat", description = "股票热度指标")
    @TableField("heat")
    private String heat;

    @Schema(name = "last_px", description = "最新价格")
    @TableField("last_px")
    private String last_px;


    @Schema(name = "market", description = "股票市场")
    @TableField("market")
    private String market;

    @Schema(name = "name", description = "股票名称")
    @TableField("name")
    private String name;

    @Schema(name = "logo_type", description = "图标类型")
    @TableField("logo_type")
    private String logo_type;

    @Schema(name = "logo_url", description = "图标的 URL 或路径")
    @TableField("logo_url")
    private String logo_url;

    @Schema(name = "px_change_rate", description = "价格变化率（如百分比）")
    @TableField("px_change_rate")
    private String px_change_rate;

    @Schema(name = "rank_diff", description = "排名变化")
    @TableField("rank_diff")
    private String rank_diff;

    @Schema(name = "created_time", description = "创建时间")
    @TableField("created_time")
    private Date created_time;

}