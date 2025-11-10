package xCloud.entity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/10 14:12
 * @ClassName MilvusRequest
 */
@Data
public class MilvusRequest {

    @NotBlank(message = "text cannot be blank")
    public String text;
}
