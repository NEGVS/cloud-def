package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户返回对象，绝不返回 password 等敏感字段
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserVO", description = "用户返回对象")
public class UserVO extends PagerModel {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "用户账号")
    private String userName;

    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "用户类型（00系统用户）")
    private String userType;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "手机号码")
    private String phonenumber;

    @Schema(description = "用户性别（0男 1女 2未知）")
    private String sex;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "帐号状态（0正常 1停用）")
    private String status;

    @Schema(description = "最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间")
    private Date loginDate;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "备注")
    private String remark;
}
