package xCloud.entity.es;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * 候选人简历ES文档实体
 * 用途：支持BM25全文检索 + 向量语义检索
 * @author Claude
 * @date 2026-08-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(indexName = "candidate_resume")
public class CandidateResumeDocument {

    /**
     * 文档ID（对应简历ID）
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * 候选人姓名
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    /**
     * 简历原始文本（用于BM25检索）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 技能标签（逗号分隔，如：Java,Spring,MySQL）
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String skills;

    /**
     * 工作经验（年）
     */
    @Field(type = FieldType.Integer)
    private Integer experience;

    /**
     * 教育背景（本科、硕士、博士）
     */
    @Field(type = FieldType.Keyword)
    private String education;

    /**
     * 期望职位
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String expectedPosition;

    /**
     * 期望薪资（单位：K/月）
     */
    @Field(type = FieldType.Integer)
    private Integer expectedSalary;

    /**
     * 当前所在城市
     */
    @Field(type = FieldType.Keyword)
    private String city;

    /**
     * 简历向量（1024维，用于语义检索）
     * 注意：ES 8.x 原生支持向量检索（knn_vector）
     */
    @Field(type = FieldType.Dense_Vector, dims = 1024)
    @JsonProperty("content_vector")
    private List<Float> contentVector;

    /**
     * 简历来源（内推、招聘网站、主动投递）
     */
    @Field(type = FieldType.Keyword)
    private String source;

    /**
     * 简历状态（待筛选、已通过、已拒绝）
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 上传时间
     */
    @Field(type = FieldType.Date)
    private Date uploadTime;

    /**
     * 最后更新时间
     */
    @Field(type = FieldType.Date)
    private Date updateTime;

    /**
     * 原始简历文件路径（OSS/本地）
     */
    @Field(type = FieldType.Keyword)
    private String filePath;
}
