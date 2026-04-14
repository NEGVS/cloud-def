package xCloud.openAiChatModel.ali.embedding;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xCloud.entity.constant.AliConstant;
import xCloud.entity.dto.VectorDTO;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description AliEmbedding
 * @Author Andy Fan
 * @Date 2025/11/7 13:49
 * @ClassName AliEmbeddingUtil
 */
@Slf4j
@Component
public class AliEmbeddingUtil {

    /**
     * 单条文本转向量
     *
     * @param inputTexts 文本
     * @return 向量；失败返回 null
     */
    public List<Double> embeddingB(String inputTexts) {
        try {
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model(AliConstant.EMBEDDING_MODEL_NAME)
                    .texts(Collections.singleton(inputTexts))
                    .build();
            TextEmbeddingResult result = new TextEmbedding().call(param);
            return result.getOutput().getEmbeddings().get(0).getEmbedding();
        } catch (NoApiKeyException e) {
            log.error("Embedding 调用失败，请检查 API Key: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 批量文本转向量（DashScope 单次最多 25 条）
     * 返回列表顺序与入参顺序一致
     *
     * @param texts 文本列表（调用方保证 size ≤ 25）
     * @return 向量列表，与入参顺序对应；失败返回 null
     */
    public List<List<Double>> embeddingBatch(List<String> texts) {
        try {
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model(AliConstant.EMBEDDING_MODEL_NAME)
                    .texts(texts)
                    .build();
            TextEmbeddingResult result = new TextEmbedding().call(param);
            return result.getOutput().getEmbeddings().stream()
                    .sorted(Comparator.comparingInt(e -> e.getTextIndex()))
                    .map(e -> e.getEmbedding())
                    .collect(Collectors.toList());
        } catch (NoApiKeyException e) {
            log.error("批量 Embedding 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 单条文本转向量（返回 VectorDTO，含 requestId）
     *
     * @param inputTexts 文本
     * @return VectorDTO；失败返回 null
     */
    public VectorDTO embedding(String inputTexts) {
        try {
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .model(AliConstant.EMBEDDING_MODEL_NAME)
                    .texts(Collections.singleton(inputTexts))
                    .build();
            TextEmbeddingResult result = new TextEmbedding().call(param);
            VectorDTO vectorDTO = new VectorDTO();
            vectorDTO.setId(result.getRequestId());
            vectorDTO.setVector(result.getOutput().getEmbeddings().get(0).getEmbedding());
            return vectorDTO;
        } catch (NoApiKeyException e) {
            log.error("Embedding 调用失败，请检查 API Key: {}", e.getMessage());
            return null;
        }
    }
}
