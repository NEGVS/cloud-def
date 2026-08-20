package xCloud.service.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 混合检索结果
 * @author Claude
 * @date 2026-08-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HybridSearchResult {

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 候选人姓名
     */
    private String name;

    /**
     * 简历内容片段
     */
    private String contentSnippet;

    /**
     * 技能标签
     */
    private String skills;

    /**
     * 工作经验（年）
     */
    private Integer experience;

    /**
     * 期望职位
     */
    private String expectedPosition;

    /**
     * BM25分数（关键词匹配）
     */
    private Double bm25Score;

    /**
     * 向量相似度分数（语义匹配）
     */
    private Double vectorScore;

    /**
     * RRF融合后的最终分数
     */
    private Double finalScore;

    /**
     * 排名位置
     */
    private Integer rank;

    /**
     * 匹配理由（可解释性）
     */
    private String matchReason;
}
