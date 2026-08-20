package xCloud.service.search;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 混合搜索Rerank服务（二次精排）
 * 使用LLM对候选结果进行相关性重排
 * 提升Top5-10的准确率
 *
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class HybridSearchRerankService {

    @Value("${ali.api-key}")
    private String apiKey;

    @Value("${ali.chat_model_name:qwen-plus}")
    private String modelName;

    /**
     * Rerank结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RerankResult {
        private String documentId;
        private String content;
        private Double originalScore;
        private Double rerankScore;
        private Integer newRank;
        private String relevanceReason;
    }

    /**
     * 对候选结果进行Rerank（使用LLM评分）
     *
     * @param query 用户查询
     * @param candidates 候选结果
     * @param topK 返回Top-K
     * @return Rerank后的结果
     */
    public List<HybridSearchResult> rerank(String query, List<HybridSearchResult> candidates, int topK) {
        try {
            if (candidates.isEmpty()) {
                return Collections.emptyList();
            }

            // ============1-批量评分（使用LLM判断相关性）============
            List<RerankResult> rerankResults = new ArrayList<>();

            for (HybridSearchResult candidate : candidates) {
                double rerankScore = scoreRelevance(query, candidate);

                RerankResult result = new RerankResult();
                result.setDocumentId(candidate.getDocumentId());
                result.setContent(candidate.getContentSnippet());
                result.setOriginalScore(candidate.getFinalScore());
                result.setRerankScore(rerankScore);

                rerankResults.add(result);
            }

            // ============2-按Rerank分数降序排序============
            rerankResults.sort((a, b) -> Double.compare(b.getRerankScore(), a.getRerankScore()));

            // ============3-更新原始结果的排名和分数============
            List<HybridSearchResult> rerankedList = new ArrayList<>();
            for (int i = 0; i < Math.min(topK, rerankResults.size()); i++) {
                RerankResult rr = rerankResults.get(i);

                // 从原始candidates中找到对应的结果
                HybridSearchResult original = candidates.stream()
                        .filter(c -> c.getDocumentId().equals(rr.getDocumentId()))
                        .findFirst()
                        .orElse(null);

                if (original != null) {
                    original.setRank(i + 1);
                    original.setFinalScore(rr.getRerankScore());
                    original.setMatchReason(rr.getRelevanceReason());
                    rerankedList.add(original);
                }
            }

            log.info("✅ Rerank完成：输入{}条，输出{}条", candidates.size(), rerankedList.size());
            return rerankedList;

        } catch (Exception e) {
            log.error("Rerank失败，返回原始排序", e);
            return candidates.stream().limit(topK).collect(Collectors.toList());
        }
    }

    /**
     * 使用LLM评估查询与文档的相关性
     * 返回0-10的分数
     */
    private double scoreRelevance(String query, HybridSearchResult candidate) {
        try {
            // ============构建评分Prompt============
            String prompt = buildScoringPrompt(query, candidate);

            // ============调用LLM============
            Generation gen = new Generation();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(modelName)
                    .messages(Collections.singletonList(
                            Message.builder()
                                    .role(Role.USER.getValue())
                                    .content(prompt)
                                    .build()
                    ))
                    .temperature(0.1f) // 低温度，更确定性
                    .maxTokens(100)
                    .build();

            GenerationResult result = gen.call(param);
            String response = result.getOutput().getChoices().get(0).getMessage().getContent();

            // ============解析分数（期望格式：分数：8 \n理由：...）============
            return parseScore(response);

        } catch (Exception e) {
            log.error("LLM评分失败，使用默认分数5.0", e);
            return 5.0;
        }
    }

    /**
     * 构建评分Prompt
     */
    private String buildScoringPrompt(String query, HybridSearchResult candidate) {
        return String.format(
                "你是一个招聘专家，请评估以下候选人与岗位需求的匹配度。\n\n" +
                "【岗位需求】\n%s\n\n" +
                "【候选人信息】\n" +
                "姓名：%s\n" +
                "技能：%s\n" +
                "经验：%d年\n" +
                "期望职位：%s\n" +
                "简历摘要：%s\n\n" +
                "请给出0-10的匹配分数（10分最高），并简要说明理由。\n" +
                "严格按照以下格式输出：\n" +
                "分数：X\n" +
                "理由：...",
                query,
                candidate.getName(),
                candidate.getSkills(),
                candidate.getExperience(),
                candidate.getExpectedPosition(),
                candidate.getContentSnippet()
        );
    }

    /**
     * 解析LLM返回的分数
     */
    private double parseScore(String response) {
        try {
            // 提取"分数：X"
            String[] lines = response.split("\n");
            for (String line : lines) {
                if (line.startsWith("分数：") || line.startsWith("分数:")) {
                    String scoreStr = line.replace("分数：", "").replace("分数:", "").trim();
                    return Double.parseDouble(scoreStr);
                }
            }

            // 如果未找到，尝试从第一行提取数字
            String firstLine = lines[0].trim();
            return Double.parseDouble(firstLine.replaceAll("[^0-9.]", ""));

        } catch (Exception e) {
            log.warn("分数解析失败，response={}", response);
            return 5.0; // 默认中等分数
        }
    }

    /**
     * 快速Rerank（仅使用规则，不调用LLM）
     * 适用于对延迟敏感的场景
     */
    public List<HybridSearchResult> fastRerank(String query, List<HybridSearchResult> candidates, int topK) {
        // ============基于简单规则调整分数============
        for (HybridSearchResult candidate : candidates) {
            double boost = 1.0;

            // 规则1：技能完全匹配加权
            if (candidate.getSkills() != null && candidate.getSkills().toLowerCase().contains(query.toLowerCase())) {
                boost += 0.3;
            }

            // 规则2：期望职位匹配加权
            if (candidate.getExpectedPosition() != null && candidate.getExpectedPosition().toLowerCase().contains(query.toLowerCase())) {
                boost += 0.2;
            }

            // 规则3：经验年限合理性
            if (candidate.getExperience() != null && candidate.getExperience() >= 3 && candidate.getExperience() <= 10) {
                boost += 0.1;
            }

            candidate.setFinalScore(candidate.getFinalScore() * boost);
        }

        // ============重新排序============
        candidates.sort((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()));

        // ============更新排名============
        for (int i = 0; i < candidates.size(); i++) {
            candidates.get(i).setRank(i + 1);
        }

        return candidates.stream().limit(topK).collect(Collectors.toList());
    }
}
