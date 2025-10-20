package xCloud.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "StockAllDTO")
@Data
public class StockAllDTO {
    @Schema(description = "id")
    private Integer id;

    @Schema(description = "block_name")
    private String block_name;

    @Schema(description = "code")
    private String code;

    @Schema(description = "exchange")
    private String exchange;

    @Schema(description = "finance_type")
    private String finance_type;

    @Schema(description = "heat")
    private String heat;

    @Schema(description = "last_px")
    private BigDecimal last_px;

    @Schema(description = "market")
    private String market;

    @Schema(description = "name")
    private String name;

    @Schema(description = "logo_type")
    private String logo_type;

    @Schema(description = "logo_url")
    private String logo_url;

    @Schema(description = "px_change_rate")
    private BigDecimal px_change_rate;

    @Schema(description = "rank_diff")
    private Integer rank_diff;

    @Schema(description = "created_time")
    private Date created_time;

    @Schema(description = "updated_time")
    private Date updated_time;

    @Schema(description = "is_active")
    private Integer is_active;

}
