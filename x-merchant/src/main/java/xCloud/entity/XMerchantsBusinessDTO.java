package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "XMerchantsBusinessDTO", description = "用于页面搜索条件")
public class XMerchantsBusinessDTO extends PagerModel {

    @Schema(name = "business_id", description = "id")
    private Integer business_id;

    @Schema(name = "merchant_id", description = "商家ID")
    private String merchant_id;

//    @Schema(name = "startDate", description = "开始营业时间")
//    private Date startDate;
//
//    @Schema(name = "endDate", description = "结束营业时间")
//    private Date endDate;

    @Schema(name = "dayOfWeek", description = "周几")
    private Integer dayOfWeek;

}
