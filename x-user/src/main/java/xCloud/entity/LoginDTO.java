package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数
 */
@Data
@Schema(name = "LoginDTO", description = "登录请求参数")
public class LoginDTO {

    @NotBlank(message = "用户账号不能为空")
    @Schema(description = "用户账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
