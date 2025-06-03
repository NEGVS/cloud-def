package xCloud.merchantsBusiness.entity;

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
@TableName(value = "x_merchants_business")
@Schema(name = "xMerchantsBusiness", description = "xMerchantsBusiness")
@Data
public class XMerchantsBusiness implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(name = "business_id", description = "id")
    @TableField("business_id")
    private Integer business_id;

    @Schema(name = "merchant_id", description = "商家ID")
    @TableField("merchant_id")
    private String merchant_id;

    @Schema(name = "startDate", description = "开始营业时间")
    @TableField("startDate")
    private Date startDate;

    @Schema(name = "endDate", description = "结束营业时间")
    @TableField("endDate")
    private Date endDate;

    @Schema(name = "dayOfWeek", description = "周几")
    @TableField("dayOfWeek")
    private Integer dayOfWeek;

}