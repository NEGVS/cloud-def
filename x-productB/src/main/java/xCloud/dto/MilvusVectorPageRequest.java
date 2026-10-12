package xCloud.dto;

import lombok.Data;

/**
 * Milvus向量分页查询请求
 *
 * @author Claude
 * @date 2026-08-27
 */
@Data
public class MilvusVectorPageRequest {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    //    @ApiModelProperty(value = "集合名称（为空则使用默认集合）", example = "andy_vectorsV2")
    private String collectionName;
}
