package xCloud.entity;

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
@Data
public class TextVectorLog  implements Serializable {

    @Serial
    private static final long serialVersionUID = -2909048452792758335L;
    private Long id;               // 可对应 Milvus 主键
    private String text;
    private String vector;   // 向量
    private LocalDateTime create_time;
    private String source;
    private String remark;
}
