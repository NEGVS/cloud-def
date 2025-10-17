package xCloud.service;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/16 17:31
 * @ClassName VectorService
 */
@Service
public class VectorService {
    private static final int VECTOR_DIMENSION = 128; // 根据模型调整

    private final HuggingFaceTokenizer tokenizer;

    public VectorService() throws IOException {
        // 加载分词器，这里使用一个简单的分词器作为示例
        // 实际应用中应使用合适的模型生成向量
        this.tokenizer = HuggingFaceTokenizer.newInstance("sentence-transformers/all-MiniLM-L6-v2");
    }

    /**
     * 将文本转换为向量
     */
    public float[] textToVector(String text) {
        // 实际应用中应该使用真正的向量模型生成向量
        // 这里只是简单模拟一个向量生成过程
        float[] vector = new float[VECTOR_DIMENSION];
        for (int i = 0; i < VECTOR_DIMENSION; i++) {
            vector[i] = (float) (text.hashCode() % (i + 1) * 0.1);
        }
        return vector;
    }

    public int getVectorDimension() {
        return VECTOR_DIMENSION;
    }
}
