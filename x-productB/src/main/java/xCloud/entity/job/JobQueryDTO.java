package xCloud.entity.job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 岗位分页查询请求参数
 */
@Schema(description = "岗位分页查询请求参数")
@Data
public class JobQueryDTO {

    @Schema(description = "当前页，默认1", example = "1")
    private Integer currentPage = 1;

    @Schema(description = "每页条数，默认10", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "岗位名称（模糊）", example = "Java")
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

    @Schema(description = "状态：1招聘中 4停招")
    private Integer status;

    @Schema(description = "是否禁用：0否 1是")
    private Integer isDisable;

    @Schema(description = "审核状态：0未审核 1通过 2不通过 100待提交审核")
    private Integer auditType;

    @Schema(description = "是否热门：1是 0否")
    private Integer isHot;
}
