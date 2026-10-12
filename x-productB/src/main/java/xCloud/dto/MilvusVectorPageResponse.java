package xCloud.dto;

import lombok.Data;

import java.util.List;

/**
 * Milvus向量分页查询响应
 *
 * @author Claude
 * @date 2026-08-27
 */
@Data
// @ApiModel("Milvus向量分页查询响应")
public class MilvusVectorPageResponse {

    // // @ApiModelProperty(value = "向量数据列表")
    private List<MilvusVectorVO> records;

    // @ApiModelProperty(value = "当前页码", example = "1")
    private Integer pageNum;

    // @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize;

    // @ApiModelProperty(value = "总记录数", example = "100")
    private Long total;

    // @ApiModelProperty(value = "总页数", example = "10")
    private Integer totalPages;

    //   // @ApiModelProperty(value = "集合名称", example = "andy_vectorsV2")
    private String collectionName;
}
