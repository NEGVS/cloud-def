package xCloud.entity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/11 11:25
 * @ClassName DropCollectionReq
 */
@Data
public class DropCollectionRequest {
    @NotBlank(message = "name cannot be blank")
    public String name;
}
