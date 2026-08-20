package xCloud.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 简历上传事件
 * @author Claude
 * @date 2026-08-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadEvent {

    /**
     * 事件ID（用于追踪）
     */
    private String eventId;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件类型（pdf、docx）
     */
    private String fileType;

    /**
     * 来源（upload、referral等）
     */
    private String source;

    /**
     * 事件时间
     */
    private Date eventTime;

    /**
     * 额外元数据
     */
    private String metadata;
}
