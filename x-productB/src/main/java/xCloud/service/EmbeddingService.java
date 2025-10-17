package xCloud.service;

import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingCreateParams;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/16 18:02
 * @ClassName EmbeddingService
 */
@Service
public class EmbeddingService {


    @Value("${openai.model}")
    private String model;


    @Value("${vector.dim}")
    private int dim;

    @Resource
    private OpenAIClient openAIClient;


    /**
     * 将单个文本转换为嵌入向量
     *
     * @param text 输入文本
     * @return 浮点数组向量（维度取决于模型，例如 ada-002 为 1536 维）
     */
    public float[] generateEmbedding(String text) {
        // 创建嵌入请求
        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .input(text)// 支持单个字符串或 List<String>
                .model("text-embedding-ada-002")// 推荐模型，维度 1536；或 "text-embedding-3-small" (维度 1536)
                .build();

        try {
            // 同步调用 API
            CreateEmbeddingResponse createEmbeddingResponse = openAIClient.embeddings().create(params);
            List<Embedding> embeddings = createEmbeddingResponse.data();

            if (!embeddings.isEmpty()) {
                Embedding embedding = embeddings.get(0);
                List<Float> embedding1 = embedding.embedding();
                float[] embeddingVector = new float[embedding1.size()];
                for (int i = 0; i < embedding1.size(); i++) {
                    embeddingVector[i] = embedding1.get(i);
                }
                return embeddingVector;
            }
        } catch (Exception e) {
            System.err.println("生成嵌入失败: " + e.getMessage());
        }
        return new float[0];  // 错误时返回空向量
    }

    /**
     * 批量生成嵌入（示例：10 句文本）
     *
     * @param texts 文本列表
     * @return Map<String, float [ ]> 文本到向量的映射
     */
    public Map<String, float[]> generateBatchEmbeddings(List<String> texts) {
        // 类似单个，但 input 为 List<String>
        EmbeddingCreateParams request = EmbeddingCreateParams.builder()
//              .input(new Input(texts))
                //todo
                .input(texts.toString())
                .model("text-embedding-ada-002")
                .build();

        try {
            CreateEmbeddingResponse response = openAIClient.embeddings().create(request);
            List<Embedding> embeddings = response.data();
            // 这里简化为 Map，实际可根据索引映射
        } catch (Exception e) {
            System.err.println("批量生成嵌入失败: " + e.getMessage());
        }
        return Map.of();  // 简化返回，实际实现映射
    }

}
