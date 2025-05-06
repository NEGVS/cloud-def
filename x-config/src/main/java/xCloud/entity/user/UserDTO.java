package xCloud.entity.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xCloud.entity.PagerModel;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
@Schema(name = "UserDTO", description = "商保方案明细-参数集合对象")
public class UserDTO extends PagerModel {
    @Schema(name = "user_id", description = "用户ID")
    private Integer user_id;

    @Schema(name = "dept_id", description = "部门ID")
    private Integer dept_id;

    @Schema(name = "user_name", description = "用户账号")
    private String user_name;

    @Schema(name = "nick_name", description = "用户昵称")
    private String nick_name;

    @Schema(name = "user_type", description = "用户类型（00系统用户）")
    private String user_type;

    @Schema(name = "email", description = "用户邮箱")
    private String email;

    @Schema(name = "phonenumber", description = "手机号码")
    private String phonenumber;

    @Schema(name = "sex", description = "用户性别（0男 1女 2未知）")
    private String sex;

    @Schema(name = "avatar", description = "头像地址")
    private String avatar;

    @Schema(name = "password", description = "密码")
    private String password;

    @Schema(name = "status", description = "帐号状态（0正常 1停用）")
    private String status;

    @Schema(name = "del_flag", description = "删除标志（0代表存在 2代表删除）")
    private String del_flag;

    @Schema(name = "login_ip", description = "最后登录IP")
    private String login_ip;

    @Schema(name = "login_date", description = "最后登录时间")
    private Date login_date;

    @Schema(name = "create_by", description = "创建者")
    private String create_by;

    @Schema(name = "create_time", description = "创建时间")
    private Date create_time;

    @Schema(name = "update_by", description = "更新者")
    private String update_by;

    @Schema(name = "update_time", description = "更新时间")
    private Date update_time;

    @Schema(name = "id", description = "主键id")
    private String id;

    @Schema(name = "username", description = "登录账号")
    private String username;

    @Schema(name = "realname", description = "真实姓名")
    private String realname;

    @Schema(name = "salt", description = "md5密码盐")
    private String salt;

    @Schema(name = "birthday", description = "生日")
    private Date birthday;

    @Schema(name = "phone", description = "电话")
    private String phone;

    @Schema(name = "org_code", description = "登录会话的机构编码")
    private String org_code;

    @Schema(name = "third_id", description = "第三方登录的唯一标识")
    private String third_id;

    @Schema(name = "third_type", description = "第三方类型")
    private String third_type;

    @Schema(name = "activiti_sync", description = "同步工作流引擎(1-同步,0-不同步)")
    private Integer activiti_sync;

    @Schema(name = "work_no", description = "工号，唯一键")
    private String work_no;

    @Schema(name = "post", description = "职务，关联职务表")
    private String post;

    @Schema(name = "telephone", description = "座机号")
    private String telephone;

    @Schema(name = "user_identity", description = "身份（1普通成员 2上级）")
    private Integer user_identity;

    @Schema(name = "depart_ids", description = "负责部门")
    private String depart_ids;

    @Schema(name = "rel_tenant_ids", description = "多租户标识")
    private String rel_tenant_ids;

    @Schema(name = "client_id", description = "设备ID")
    private String client_id;


}
