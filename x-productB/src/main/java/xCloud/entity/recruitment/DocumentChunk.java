package xCloud.entity.recruitment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PDF 文档切块实体，存储于 MySQL，用于关键词检索和内容回显
 *
 * 对应表：recruitment_chunk
 * CREATE TABLE recruitment_chunk (
 *   id           BIGINT       PRIMARY KEY,
 *   content      TEXT         NOT NULL,
 *   source       VARCHAR(500) NOT NULL COMMENT '文件名',
 *   doc_type     VARCHAR(100) NOT NULL COMMENT 'company_doc / job_info',
 *   chunk_index  INT          NOT NULL COMMENT '块序号',
 *   page_num     INT          NOT NULL DEFAULT 0,
 *   create_time  DATETIME     NOT NULL
 * );
 */
@Data
@TableName("recruitment_chunk")
public class DocumentChunk {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String content;

    private String source;

    private String docType;

    private Integer chunkIndex;

    private Integer pageNum;

    private LocalDateTime createTime;

    /** 检索得分（非持久化，仅用于排序） */
    private transient Float score;

    /** RRF 排名（非持久化） */
    private transient Integer rank;
}
