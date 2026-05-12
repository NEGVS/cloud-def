package xCloud.entity.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位列表展示 VO
 */
@Schema(description = "岗位列表返回数据")
@Data
public class JobVO {

    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "岗位名称")
    private String name;

    @Schema(description = "岗位类型：1全职 2兼职")
    private Integer jobNature;

    @Schema(description = "岗位属性：1商户岗位 2自营岗位")
    private Integer jobType;

    @Schema(description = "招聘类型：1直招 2派遣 3代招")
    private Integer recruitmentType;

    @Schema(description = "一级分类ID")
    private Integer oneCategoryId;

    @Schema(description = "二级分类ID")
    private Integer categoryId;

    @Schema(description = "门店（公司）id")
    private Integer companyId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "全部招聘人数")
    private Integer hireAll;

    @Schema(description = "性别要求：0不限 1男 2女")
    private Integer genderRequire;

    @Schema(description = "学历要求：0不限 1初中及以下 2初中 3高中 4大专 5本科 6研究生")
    private Integer educationRequire;

    @Schema(description = "年龄要求最小值")
    private Integer ageMin;

    @Schema(description = "年龄要求最大值")
    private Integer ageMax;

    @Schema(description = "食宿情况：1包吃包住 2包吃 3包住 4不包吃住")
    private Integer accommodation;

    @Schema(description = "社保公积金：0无 1五险 2五险一金 3五险二金")
    private Integer socialSecurityFund;

    @Schema(description = "奖金补贴，逗号分隔")
    private String bonusSubsidy;

    @Schema(description = "企业福利，逗号分隔")
    private String welfares;

    @Schema(description = "自定义标签")
    private String jobTag;

    @Schema(description = "状态：1招聘中 4停招")
    private Integer status;

    @Schema(description = "是否禁用：0否 1是")
    private Integer isDisable;

    @Schema(description = "审核状态：0未审核 1通过 2不通过 100待提交审核")
    private Integer auditType;

    @Schema(description = "是否热门：1是 0否")
    private Integer isHot;

    @Schema(description = "道具状态：0无 1生效 2暂停")
    private Integer propStatus;

    @Schema(description = "道具类型：1置顶急招 2人才炸弹")
    private Integer propType;

    @Schema(description = "最后一次发布时间")
    private LocalDateTime releaseTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
