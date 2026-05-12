package xCloud.entity.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 岗位新增/编辑请求 DTO
 * （新增和更新共用，更新时需传 id）
 */
@Schema(description = "岗位新增/编辑请求参数")
@Data
public class JobDTO {

    @Schema(description = "主键ID（新增时不传，更新时必传）")
    private Integer id;

    @Schema(description = "岗位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java开发工程师")
    private String name;

    @Schema(description = "岗位类型：1全职 2兼职", example = "1")
    private Integer jobNature;

    @Schema(description = "岗位属性：1商户岗位 2自营岗位", example = "1")
    private Integer jobType;

    @Schema(description = "岗位一级分类ID", example = "10")
    private Integer oneCategoryId;

    @Schema(description = "岗位二级分类ID", example = "101")
    private Integer categoryId;

    @Schema(description = "招聘类型：1直招 2派遣 3代招", example = "1")
    private Integer recruitmentType;

    @Schema(description = "商户端用户id", example = "1001")
    private Integer userId;

    @Schema(description = "奖金补贴，逗号分隔，如 1,2,3", example = "1,2,11")
    private String bonusSubsidy;

    @Schema(description = "社保公积金：0无 1五险 2五险一金 3五险二金", example = "2")
    private Integer socialSecurityFund;

    @Schema(description = "企业福利，逗号分隔，如 1,2,3", example = "5,9")
    private String welfares;

    @Schema(description = "食宿情况：1包吃包住 2包吃 3包住 4不包吃住", example = "1")
    private Integer accommodation;

    @Schema(description = "岗位详情（富文本）")
    private String jobDetail;

    @Schema(description = "年龄要求最小值，0不限", example = "18")
    private Integer ageMin;

    @Schema(description = "年龄要求最大值，0不限", example = "35")
    private Integer ageMax;

    @Schema(description = "性别要求：0不限 1男 2女", example = "0")
    private Integer genderRequire;

    @Schema(description = "学历要求：0不限 1初中及以下 2初中 3高中 4大专 5本科 6研究生", example = "4")
    private Integer educationRequire;

    @Schema(description = "其他要求")
    private String otherRequire;

    @Schema(description = "门店（公司）id", example = "200")
    private Integer companyId;

    @Schema(description = "岗位主品牌ID，0未指定", example = "0")
    private Integer brandId;

    @Schema(description = "所属群ID", example = "0")
    private Integer groupId;

    @Schema(description = "全部招聘人数", example = "5")
    private Integer hireAll;

    @Schema(description = "岗位照片id，逗号分隔", example = "11,12,13")
    private String imgIds;

    @Schema(description = "视频ID", example = "0")
    private Integer videoId;

    @Schema(description = "视频封面图ID", example = "0")
    private Integer videoImageId;

    @Schema(description = "自定义标签", example = "急招,高薪")
    private String jobTag;

    @Schema(description = "是否开放手机：1开放 2未开放", example = "1")
    private Integer isOpenPhone;
}
