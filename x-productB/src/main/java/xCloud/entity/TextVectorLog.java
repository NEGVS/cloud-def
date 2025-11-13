package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/11 13:54
 * @ClassName TextVectorLog
 */
@Schema(description = "文本向量日志")
@Data
public class TextVectorLog  implements Serializable {

    @Serial
    private static final long serialVersionUID = -2909048452792758335L;

    @Schema(description = "主键")
    private Long id;               // 可对应 Milvus 主键
    @Schema(description = "文本")
    private String text;
    @Schema(description = "向量")
    private String vector;   // 向量
    @Schema(description = "创建时间")
    private LocalDateTime create_time;
    @Schema(description = "来源")
    private String source;
    @Schema(description = "备注")
    private String remark;
}
