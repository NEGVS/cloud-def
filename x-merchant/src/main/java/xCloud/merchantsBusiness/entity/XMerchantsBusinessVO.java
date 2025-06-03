package xCloud.merchantsBusiness.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xCloud.entity.PagerModel;

import java.util.Date;

@Data
@Schema(name = "XMerchantsBusinessVO", description = "商保方案明细-参数集合对象")
public class XMerchantsBusinessVO extends PagerModel {
    @Schema(name = "business_id", description = "id")
    private Integer business_id;

    @Schema(name = "merchant_id", description = "商家ID")
    private String merchant_id;

    @Schema(name = "startDate", description = "开始营业时间")
    private Date startDate;

    @Schema(name = "endDate", description = "结束营业时间")
    private Date endDate;

    @Schema(name = "dayOfWeek", description = "周几")
    private Integer dayOfWeek;


}
