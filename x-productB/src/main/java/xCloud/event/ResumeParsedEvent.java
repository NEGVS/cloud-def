package xCloud.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 简历解析完成事件
 * @author Claude
 * @date 2026-08-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeParsedEvent {

    private String eventId;
    private Long resumeId;
    private String name;
    private String phone;
    private String email;
    private String skills;
    private Integer workYears;
    private String expectedPosition;
    private Integer expectedSalary;
    private String rawContent;
    private Date eventTime;
}
