package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录成功返回：JWT令牌 + 基础用户信息（不含密码）
 */
@Data
@Schema(name = "LoginVO", description = "登录成功返回")
public class LoginVO {

    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户账号")
    private String userName;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "头像地址")
    private String avatar;
}
