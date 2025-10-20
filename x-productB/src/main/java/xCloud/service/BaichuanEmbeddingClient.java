package xCloud.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/20 11:40
 * @ClassName BaichuanEmbeddingClient
 */
@Component
public class BaichuanEmbeddingClient {

    @Resource
    private WebClient webClient;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${baichuan.api.key}")
    private String apiKey;

    @Value("${baichuan.api.url}")
    private String url;
//    sdakldfsj

    private static String MODEL_NAME = "Baichuan-Text-Embedding";

    private static int BATCH_SIZE = 16; // 接口最大批量处理数


    /**
     * 将单个文本转换为向量
     *
     * @param text 输入文本
     * @return 1024维向量
     */
    public Mono<List<Float>> embedText(String text) {
        return embedTexts(List.of(text))
                .map(embeddings -> embeddings.get(0));
    }

    /**
     * 将多个文本转换为向量
     *
     * @param texts 文本列表
     * @return 向量列表，与输入文本顺序一致
     */
    public Mono<List<List<Float>>> embedTexts(List<String> texts) {
        // 检查输入合法性
        if (texts == null || texts.isEmpty()) {
            return Mono.error(new IllegalArgumentException("输入文本不能为空"));
        }

        // 按批次处理（最大16个文本）
        return Flux.fromIterable(splitIntoBatches(texts, BATCH_SIZE))
                .flatMap(this::processBatch)          // Mono<List<List<Float>>>
                .flatMapIterable(batch -> batch)      // => Flux<List<Float>>
                .collectList();                       // => Mono<List<List<Float>>>
    }

    /**
     * 将列表分割为指定大小的批次
     */
    private List<List<String>> splitIntoBatches(List<String> list, int batchSize) {
        return Flux.fromIterable(list)
                .buffer(batchSize)
                .collectList()
                .block(); // 这里使用block是安全的，因为是在内存中分割列表
    }

    /**
     * 处理单个批次的文本
     */
    private Mono<List<List<Float>>> processBatch(List<String> batchTexts) {
        // 构建请求体
        EmbeddingRequest request = new EmbeddingRequest();
        request.setModel(MODEL_NAME);
        request.setInput(batchTexts);

        return webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .map(response -> response.getData().stream()
                        // 按索引排序确保顺序一致
                        .sorted((a, b) -> Integer.compare(a.getIndex(), b.getIndex()))
                        .map(EmbeddingData::getEmbedding)
                        .collect(Collectors.toList()));
    }

    // 请求体模型
    @Data
    static class EmbeddingRequest {
        private String model;
        private Object input; // 支持单个字符串或字符串列表
    }

    // 响应体模型
    @Data
    static class EmbeddingResponse {
        private List<EmbeddingData> data;
        private String object;
        private String model;
        private Usage usage;
    }

    @Data
    static class EmbeddingData {
        private int index;
        private List<Float> embedding;
        @JsonProperty("object")
        private String objectType;
    }

    @Data
    static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;
    }
}
