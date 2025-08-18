package xCloud.aRedis.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 熟人直聘用户主表
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
@Accessors(chain = true)
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = -5812650975540158959L;
    /**
     *
     */
    @TableId
    private Integer id;

    /**
     * 姓名
     */
    private String name;

    private String nickName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 密码
     */
    private String password;

    /**
     * 性别 1:男 2：女 0：未知
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 是否实名 0:否 1:是1
     */
    private Integer isRealName;

    /**
     * 当前登录角色1-C端 2-商户端
     */
    private Integer loginRole;

    /**
     * 首次选择的身份时间
     */
    private Date firstRoleTime;

    /**
     * 首次选择的身份
     */
    private Integer firstRole;

    /**
     * 现居地址
     */
    private String address;

    /**
     * 居住地址经度
     */
    private String addressLon;

    /**
     * 居住地址纬度
     */
    private String addressLat;

    /**
     * 头像地址
     */
    private String headerUrl;

    /**
     * 自我介绍
     */
    private String selfIntroduce;

    /**
     * 微信号,唯一不变那个
     */
    private String wechatId;

    /**
     * 是否绑定微信 0.未绑定 1.已绑定
     */
    private Integer isBindWechat;

    /**
     * 微信头像图片地址
     */
    private String wechatAvatar;

    /**
     * 微信 ID
     */
    private String wechatOpenId;

    /**
     * 微信昵称
     */
    private String wechatName;

    /**
     * 注册 IP
     */
    private String regIp;

    /**
     * 渠道号
     */
//    private String channelNo;

    /**
     * 极光推送 ID
     */
    private String registerId;


    /**
     * 状态：1：正常，2冻结
     */
    private Integer status;
    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 首次登录时间
     */
    private Date firstLoginTime;

    /**
     * 激活时间（首次app登录时间）
     */
    private Date activateTime;

    /**
     * 平台类型 ios android h5 weixinmp
     */
    private String platform;

    /**
     * 职位 字典值参考 common_dict->b_position
     */
    private Integer positionId;

    /**
     * 职位中文
     */
    private String positionZh;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 删除时间
     */
    private Date deletedAt;

    /**
     * 求职意向： 1.积极看工作 2.随便看看 3.暂时不换工作
     */
    private Integer jobWantStatus;
    /**
     * 0:非摘星台会员 1:是摘星台会员
     */
    private Integer isZxtUser;


    /* 实名认证通过时间 */
    private Date realNameTime;
    /* 实名认证方式 1.第三方 2.人工认证 */
    private Integer realNameType;

}