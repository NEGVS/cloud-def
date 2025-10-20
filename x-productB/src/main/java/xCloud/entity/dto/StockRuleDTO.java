package xCloud.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "StockRuleDTO")
public class StockRuleDTO {
    @Schema(description = "id")
    private Integer id;

    @Schema(description = "user_id")
    private Integer user_id;

    @Schema(description = "code")
    private String code;

    @Schema(description = "rule_name")
    private String rule_name;

    @Schema(description = "rule_description")
    private String rule_description;

    @Schema(description = "summary")
    private String summary;

    @Schema(description = "notes")
    private String notes;

    @Schema(description = "is_active")
    private Integer is_active;

    @Schema(description = "created_time")
    private Date created_time;

    @Schema(description = "updated_time")
    private Date updated_time;

}
