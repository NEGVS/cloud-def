package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Schema(name = "MerchantsDTO", description = "商保方案明细-参数集合对象")
public class MerchantsDTO extends PagerModel {

    @Schema(name = "merchant_id", description = "商家ID")
    private Integer merchant_id;

    @Schema(name = "user_id", description = "商家用户ID")
    private String user_id;

    @Schema(name = "name", description = "商家名称")
    private String name;

}
