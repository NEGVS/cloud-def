package xCloud.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 简历向量化完成事件
 * @author Claude
 * @date 2026-08-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeVectorizedEvent {

    private String eventId;
    private Long resumeId;
    private List<Float> vector;
    private String collectionName;
    private Date eventTime;
}
