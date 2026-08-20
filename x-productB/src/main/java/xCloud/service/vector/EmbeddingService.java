package xCloud.service.vector;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Embedding服务（文本向量化）
 * 使用阿里云text-embedding-v4模型（1024维）
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class EmbeddingService {

    @Value("${ali.api-key}")
    private String apiKey;

    @Value("${ali.embedding_model_name:text-embedding-v4}")
    private String modelName;

    /**
     * 文本向量化（单条）
     * @param text 输入文本
     * @return 向量（1024维）
     */
    public List<Float> embed(String text) {
        return embedBatch(Collections.singletonList(text)).get(0);
    }

    /**
     * 文本向量化（批量，提升性能）
     * @param texts 输入文本列表
     * @return 向量列表
     */
    public List<List<Float>> embedBatch(List<String> texts) {
        try {
            // ============1-构建请求参数============
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model(modelName)
                    .texts(texts)
                    .apiKey(apiKey)
                    .build();

            // ============2-调用阿里云API============
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult result = textEmbedding.call(param);

            // ============3-提取向量============
            return result.getOutput().getEmbeddings().stream()
                    .map(embedding -> embedding.getEmbedding().stream()
                            .map(Double::floatValue)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("向量化失败：texts.size={}", texts.size(), e);
            throw new RuntimeException("文本向量化失败", e);
        }
    }

    /**
     * 计算两个向量的余弦相似度
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 相似度 [0, 1]
     */
    public double cosineSimilarity(List<Float> vec1, List<Float> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("向量维度不一致");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
