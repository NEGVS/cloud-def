package xCloud.dto;

import lombok.Data;

import java.util.List;

/**
 * Milvus向量数据VO
 *
 * @author Claude
 * @date 2026-08-27
 */
@Data
// @ApiModel("Milvus向量数据")
public class MilvusVectorVO {

    // @ApiModelProperty(value = "向量ID", example = "123456")
    private Long id;

    // @ApiModelProperty(value = "文本内容（超过5000字符会截断）", example = "这是一段简历文本内容...")
    private String text;

    // @ApiModelProperty(value = "向量数据（维度1024的浮点数组）")
    private List<Float> vector;

    // @ApiModelProperty(value = "文本原始长度", example = "8000")
    private Integer originalTextLength;

    // @ApiModelProperty(value = "是否截断", example = "true")
    private Boolean truncated;
}
