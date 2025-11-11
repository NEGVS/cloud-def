package xCloud.entity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/11 11:30
 * @ClassName MilvusSearchReq
 */
@Data
public class MilvusSearchReq {

    @NotBlank(message = "text cannot be blank")
    public String text;
}
