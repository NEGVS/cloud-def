package xCloud.openAiChatModel.ali.embedding;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.stereotype.Component;
import xCloud.entity.dto.VectorDTO;

import java.util.Collections;
import java.util.List;

/**
 * @Description AliEmbedding
 * @Author Andy Fan
 * @Date 2025/11/7 13:49
 * @ClassName AliEmbeddingUtil
 */
@Component
public class AliEmbeddingUtil {
    /**
     * 文本 转 向量
     *
     * @param inputTexts 文本
     */
    public List<Double> embeddingB(String inputTexts) {
        // 手动从环境变量获取并设置 API Key
        String apiKey = System.getenv("DASHSCOPE_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("环境变量 DASHSCOPE_API_KEY 未设置");
        }

        try {// 构建请求参数
            TextEmbeddingParam param = TextEmbeddingParam
                    .builder()
                    .model("text-embedding-v4")
                    .texts(Collections.singleton(inputTexts))// 输入文本
                    .build();

            // 创建模型实例并调用
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult result = textEmbedding.call(param);
            return result.getOutput().getEmbeddings().get(0).getEmbedding();
        } catch (NoApiKeyException e) {
            // 捕获并处理API Key未设置的异常
            System.err.println("调用 API 时发生异常: " + e.getMessage());
            System.err.println("请检查您的 API Key 是否已正确配置。");
        }
        return null;

    }

    /**
     * 文本 转 向量
     *
     * @param inputTexts 文本
     */
    public VectorDTO embedding(String inputTexts) {
        // 手动从环境变量获取并设置 API Key
        String apiKey = System.getenv("DASHSCOPE_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("环境变量 DASHSCOPE_API_KEY 未设置");
        }

        try {
            // 构建请求参数
            TextEmbeddingParam param = TextEmbeddingParam
                    .builder()
                    .model("text-embedding-v4")
                    .texts(Collections.singleton(inputTexts))// 输入文本
                    .build();

            // 创建模型实例并调用
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult result = textEmbedding.call(param);

            VectorDTO vectorDTO = new VectorDTO();
            vectorDTO.setId(result.getRequestId());
            vectorDTO.setVector(result.getOutput().getEmbeddings().get(0).getEmbedding());
            return vectorDTO;
        } catch (NoApiKeyException e) {
            // 捕获并处理API Key未设置的异常
            System.err.println("调用 API 时发生异常: " + e.getMessage());
            System.err.println("请检查您的 API Key 是否已正确配置。");
        }
        return null;

    }

}
