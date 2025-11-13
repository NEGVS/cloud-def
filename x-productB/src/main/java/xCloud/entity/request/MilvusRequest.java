package xCloud.entity.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/10 14:12
 * @ClassName MilvusRequest
 */
@Schema(description = "Milvus 请求参数")
@Data
public class MilvusRequest {

    @NotBlank(message = "text cannot be blank")
    @Schema(description = "文本")
    public String text;
}
