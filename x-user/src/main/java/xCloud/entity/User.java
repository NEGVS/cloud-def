package xCloud.entity;

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

/**
 * 用户信息表 sys_user 实体，字段严格对齐 docs/sql.sql
 */
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@TableName(value = "sys_user")
@Schema(name = "User", description = "用户信息表")
@Data
public class User implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("dept_id")
    @Schema(description = "部门ID")
    private Long deptId;

    @TableField("user_name")
    @Schema(description = "用户账号")
    private String userName;

    @TableField("nick_name")
    @Schema(description = "用户昵称")
    private String nickName;

    @TableField("user_type")
    @Schema(description = "用户类型（00系统用户）")
    private String userType;

    @TableField("email")
    @Schema(description = "用户邮箱")
    private String email;

    @TableField("phonenumber")
    @Schema(description = "手机号码")
    private String phonenumber;

    @TableField("sex")
    @Schema(description = "用户性别（0男 1女 2未知）")
    private String sex;

    @TableField("avatar")
    @Schema(description = "头像地址")
    private String avatar;

    @TableField("password")
    @Schema(description = "密码")
    private String password;

    @TableField("status")
    @Schema(description = "帐号状态（0正常 1停用）")
    private String status;

    @TableField("del_flag")
    @Schema(description = "删除标志（0代表存在 2代表删除）")
    private String delFlag;

    @TableField("login_ip")
    @Schema(description = "最后登录IP")
    private String loginIp;

    @TableField("login_date")
    @Schema(description = "最后登录时间")
    private Date loginDate;

    @TableField("create_by")
    @Schema(description = "创建者")
    private String createBy;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField("update_by")
    @Schema(description = "更新者")
    private String updateBy;

    @TableField("update_time")
    @Schema(description = "更新时间")
    private Date updateTime;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}
