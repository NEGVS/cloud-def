package xCloud.service;

import org.springframework.stereotype.Service;


/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/16 17:31
 * @ClassName VectorService
 */
@Service
public class VectorService {

    private static final int VECTOR_DIMENSION = 128; // 根据模型调整




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

}
