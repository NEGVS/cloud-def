-- 招聘文档块表
-- 存储 PDF 切分后的文本块，用于关键词检索（Hybrid RAG 关键词路）
CREATE TABLE IF NOT EXISTS recruitment_chunk
(
    id          BIGINT       NOT NULL PRIMARY KEY COMMENT '雪花算法主键（与 Milvus id 一致）',
    content     TEXT         NOT NULL COMMENT '文档块原始文本',
    source      VARCHAR(500) NOT NULL COMMENT '来源文件名',
    doc_type    VARCHAR(100) NOT NULL COMMENT '文档类型：company_doc / job_info',
    chunk_index INT          NOT NULL DEFAULT 0 COMMENT '块在文档内的序号（从0开始）',
    page_num    INT          NOT NULL DEFAULT 0 COMMENT '所在页码（0表示未知）',
    create_time DATETIME     NOT NULL COMMENT '入库时间',
    INDEX idx_doc_type (doc_type),
    INDEX idx_source (source(200)),
    INDEX idx_create_time (create_time),
    FULLTEXT INDEX ft_content (content) WITH PARSER ngram  -- 支持中文全文检索（MySQL 5.7+）
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT '招聘文档块表';
