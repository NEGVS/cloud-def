package xCloud.feignClient.userClient;

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
@TableName(value = "sys_user")
@Schema(name = "user", description = "user")
@Data
public class User implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.AUTO)
    @Schema(name = "user_id", description = "用户ID")
    private Integer user_id;

    @Schema(name = "dept_id", description = "部门ID")
    @TableField("dept_id")
    private Integer dept_id;

    @Schema(name = "user_name", description = "用户账号")
    @TableField("user_name")
    private String user_name;

    @Schema(name = "nick_name", description = "用户昵称")
    @TableField("nick_name")
    private String nick_name;

    @Schema(name = "user_type", description = "用户类型（00系统用户）")
    @TableField("user_type")
    private String user_type;

    @Schema(name = "email", description = "用户邮箱")
    @TableField("email")
    private String email;

    @Schema(name = "phonenumber", description = "手机号码")
    @TableField("phonenumber")
    private String phonenumber;

    @Schema(name = "sex", description = "用户性别（0男 1女 2未知）")
    @TableField("sex")
    private String sex;

    @Schema(name = "avatar", description = "头像地址")
    @TableField("avatar")
    private String avatar;

    @Schema(name = "password", description = "密码")
    @TableField("password")
    private String password;

    @Schema(name = "status", description = "帐号状态（0正常 1停用）")
    @TableField("status")
    private String status;

    @Schema(name = "del_flag", description = "删除标志（0代表存在 2代表删除）")
    @TableField("del_flag")
    private String del_flag;

    @Schema(name = "login_ip", description = "最后登录IP")
    @TableField("login_ip")
    private String login_ip;

    @Schema(name = "login_date", description = "最后登录时间")
    @TableField("login_date")
    private Date login_date;

    @Schema(name = "create_by", description = "创建者")
    @TableField("create_by")
    private String create_by;

    @Schema(name = "create_time", description = "创建时间")
    @TableField("create_time")
    private Date create_time;

    @Schema(name = "update_by", description = "更新者")
    @TableField("update_by")
    private String update_by;

    @Schema(name = "update_time", description = "更新时间")
    @TableField("update_time")
    private Date update_time;

    @Schema(name = "id", description = "主键id")
    @TableField("id")
    private String id;

    @Schema(name = "username", description = "登录账号")
    @TableField("username")
    private String username;

    @Schema(name = "realname", description = "真实姓名")
    @TableField("realname")
    private String realname;

    @Schema(name = "salt", description = "md5密码盐")
    @TableField("salt")
    private String salt;

    @Schema(name = "birthday", description = "生日")
    @TableField("birthday")
    private Date birthday;

    @Schema(name = "phone", description = "电话")
    @TableField("phone")
    private String phone;

    @Schema(name = "org_code", description = "登录会话的机构编码")
    @TableField("org_code")
    private String org_code;

    @Schema(name = "third_id", description = "第三方登录的唯一标识")
    @TableField("third_id")
    private String third_id;

    @Schema(name = "third_type", description = "第三方类型")
    @TableField("third_type")
    private String third_type;

    @Schema(name = "activiti_sync", description = "同步工作流引擎(1-同步,0-不同步)")
    @TableField("activiti_sync")
    private Integer activiti_sync;

    @Schema(name = "work_no", description = "工号，唯一键")
    @TableField("work_no")
    private String work_no;

    @Schema(name = "post", description = "职务，关联职务表")
    @TableField("post")
    private String post;

    @Schema(name = "telephone", description = "座机号")
    @TableField("telephone")
    private String telephone;

    @Schema(name = "user_identity", description = "身份（1普通成员 2上级）")
    @TableField("user_identity")
    private Integer user_identity;

    @Schema(name = "depart_ids", description = "负责部门")
    @TableField("depart_ids")
    private String depart_ids;

    @Schema(name = "rel_tenant_ids", description = "多租户标识")
    @TableField("rel_tenant_ids")
    private String rel_tenant_ids;

    @Schema(name = "client_id", description = "设备ID")
    @TableField("client_id")
    private String client_id;

}