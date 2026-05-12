package xCloud.entity.job;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位主表
 *
 * @TableName job
 */
@Schema(description = "岗位主表实体")
@TableName(value = "job")
@Data
public class Job implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    /** 岗位名称 */
    @Schema(description = "岗位名称")
    private String name;

    /**
     * 岗位类型（工作性质）
     * 1:全职 2:兼职
     */
    @Schema(description = "岗位类型：1全职 2兼职")
    private Integer jobNature;

    /**
     * 岗位属性
     * 1:商户岗位 2:自营岗位
     */
    @Schema(description = "岗位属性：1商户岗位 2自营岗位")
    private Integer jobType;

    /** 岗位一级分类ID */
    @Schema(description = "岗位一级分类ID")
    private Integer oneCategoryId;

    /** 岗位二级分类ID */
    @Schema(description = "岗位二级分类ID")
    private Integer categoryId;

    /**
     * 招聘类型
     * 1:直招 2:派遣 3:代招
     */
    @Schema(description = "招聘类型：1直招 2派遣 3代招")
    private Integer recruitmentType;

    /** 商户端用户id */
    @Schema(description = "商户端用户id")
    private Integer userId;

    /**
     * 奖金补贴
     * 1餐补 2房补 3保底工资 4加班费 5话补 6晚班补贴 7高温补贴
     * 8交通补贴 9底薪加提成 10绩效奖金 11年终奖 12年底双薪
     * 13全勤奖 14工龄奖 15法定节假日三薪（逗号分隔）
     */
    @Schema(description = "奖金补贴，逗号分隔数字，如 1,2,3")
    private String bonusSubsidy;

    /**
     * 社保公积金
     * 0无 1五险 2五险一金 3五险二金
     */
    @Schema(description = "社保公积金：0无 1五险 2五险一金 3五险二金")
    private Integer socialSecurityFund;

    /**
     * 企业福利（逗号分隔）
     * 1员工旅游 2环境好 3老板好 4帅哥美女多 5不加班
     * 6就近分配 7免费培训 8员工聚餐 9晋升空间大 10生日福利
     * 11宿舍有空调 12有无线网 13免费工装
     */
    @Schema(description = "企业福利，逗号分隔数字，如 1,2,3")
    private String welfares;

    /**
     * 食宿情况
     * 1包吃包住 2包吃 3包住 4不包吃住
     */
    @Schema(description = "食宿情况：1包吃包住 2包吃 3包住 4不包吃住")
    private Integer accommodation;

    /** 岗位详情 */
    @Schema(description = "岗位详情（富文本）")
    private String jobDetail;

    /** 年龄要求-最小 */
    @Schema(description = "年龄要求最小值，0表示不限")
    private Integer ageMin;

    /** 年龄要求-最大 */
    @Schema(description = "年龄要求最大值，0表示不限")
    private Integer ageMax;

    /**
     * 性别要求
     * 0不限 1男 2女
     */
    @Schema(description = "性别要求：0不限 1男 2女")
    private Integer genderRequire;

    /**
     * 学历要求
     * 0不限 1初中及以下 2初中 3高中 4大专 5本科 6研究生
     */
    @Schema(description = "学历要求：0不限 1初中及以下 2初中 3高中 4大专 5本科 6研究生")
    private Integer educationRequire;

    /** 其他要求 */
    @Schema(description = "其他要求")
    private String otherRequire;

    /** 门店（公司）id */
    @Schema(description = "门店（公司）id")
    private Integer companyId;

    /** 岗位主品牌ID */
    @Schema(description = "岗位主品牌ID，0表示未指定")
    private Integer brandId;

    /** 所属群ID */
    @Schema(description = "所属群ID")
    private Integer groupId;

    /** 全部招聘人数 */
    @Schema(description = "全部招聘人数")
    private Integer hireAll;

    /** 岗位照片id，逗号分隔，对应image表 */
    @Schema(description = "岗位照片id，逗号分隔")
    private String imgIds;

    /** 视频ID */
    @Schema(description = "视频ID，对应image表主键，type=video")
    private Integer videoId;

    /** 视频封面图ID */
    @Schema(description = "视频封面图ID，对应image表主键，type=video_image")
    private Integer videoImageId;

    /**
     * 岗位创建来源
     * 1:后台 2:小程序 3:app 4:摘星台同步
     */
    @Schema(description = "岗位来源：1后台 2小程序 3app 4摘星台同步")
    private Integer source;

    /** 最近一次审核人ID */
    @Schema(description = "最近一次审核人ID（操作禁用/启用管理员ID）")
    private Integer lastAuditorId;

    /** 下架理由 */
    @Schema(description = "下架理由")
    private String downReason;

    /** 禁用理由 */
    @Schema(description = "禁用理由")
    private String disableReason;

    /** 最后一次操作禁用A端ID */
    @Schema(description = "最后一次操作禁用的A端管理员ID")
    private Integer updatedDisableAdminId;

    /** 最后一次操作状态A端ID */
    @Schema(description = "最后一次操作状态的A端管理员ID")
    private Integer updatedStatusAdminId;

    /** 最后一次更新状态的用户ID */
    @Schema(description = "最后一次更新状态的用户ID（用户或管理员）")
    private Integer updatedStatusUserId;

    /**
     * 状态
     * 1:招聘中 4:停招
     */
    @Schema(description = "状态：1招聘中 4停招")
    private Integer status;

    /**
     * 是否禁用
     * 0否 1是
     */
    @Schema(description = "是否禁用：0否 1是")
    private Integer isDisable;

    /**
     * 是否开放手机
     * 1开放 2未开放
     */
    @Schema(description = "是否开放手机：1开放 2未开放")
    private Integer isOpenPhone;

    /** 最后一次发布时间 */
    @Schema(description = "最后一次发布时间")
    private LocalDateTime releaseTime;

    /** 审核时间 */
    @Schema(description = "审核时间")
    private LocalDateTime auditAt;

    /**
     * 岗位审核状态
     * 0未审核 1通过 2不通过 100待提交审核
     */
    @Schema(description = "审核状态：0未审核 1通过 2不通过 100待提交审核")
    private Integer auditType;

    /** 最后一次审核备注 */
    @Schema(description = "最后一次审核备注")
    private String auditRemark;

    /** 审核提交时间 */
    @Schema(description = "审核提交时间")
    private LocalDateTime auditSubmitAt;

    /** A端创建人ID */
    @Schema(description = "A端创建人ID")
    private Integer createdAdminId;

    /** 最近一次A端编辑人 */
    @Schema(description = "最近一次A端编辑人ID")
    private Integer updatedAdminId;

    /** 最近一次B端编辑人 */
    @Schema(description = "最近一次B端编辑人ID")
    private Integer updatedUserId;

    /** 最后一次提交审核的审核日志ID */
    @Schema(description = "最后一次提交审核的审核日志ID")
    private Integer lastJobAuditLogId;

    /** 自定义标签 */
    @Schema(description = "自定义标签")
    private String jobTag;

    /**
     * 是否热门岗位
     * 1是 0否
     */
    @Schema(description = "是否热门：1是 0否")
    private Integer isHot;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /** 删除时间（软删除） */
    @TableLogic
    @Schema(description = "删除时间，null表示未删除")
    private LocalDateTime deletedAt;

    /**
     * 道具状态
     * 0无 1生效 2暂停
     */
    @Schema(description = "道具状态：0无 1生效 2暂停")
    private Integer propStatus;

    /**
     * 道具类型
     * 1置顶急招 2人才炸弹
     */
    @Schema(description = "道具类型：1置顶急招 2人才炸弹")
    private Integer propType;

    /**
     * 道具阶段
     * 1/2/3
     */
    @Schema(description = "道具阶段：1/2/3")
    private Integer propStage;

    /** 关联道具订单ID */
    @Schema(description = "关联道具订单ID")
    private Long propOrderId;

    /** 道具生效时间 */
    @Schema(description = "道具生效时间")
    private LocalDateTime propEffectTime;

    /**
     * 当前阶段是否达标
     * 0未达标 1已达标
     */
    @Schema(description = "当前阶段是否达标：0未达标 1已达标")
    private Integer propReached;
}
