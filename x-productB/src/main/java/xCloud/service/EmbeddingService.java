package xCloud.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingCreateParams;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/16 18:02
 * @ClassName EmbeddingService
 */
@Slf4j
@Service
public class EmbeddingService {


    @Value("${openai.model}")
    private String model;

    @Value("${baichuan.api.url}")
    private String bai_url;

    @Value("${baichuan.api.key}")
    private String bai_key;

    @Value("${vector.dim}")
    private int dim;

    @Resource
    private OpenAIClient openAIClient;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private WebClient webClient;


    /**
     * 异步获取单个文本的 embedding（响应式版本）。
     *
     * @param text 输入文本。
     * @return Mono<List < Double>>：异步返回 1024 维 embedding 向量。
     */
    public Mono<List<Double>> getEmbedding(String text) {
        return getEmbeddings(List.of(text))
                .map(embeddings -> embeddings.get(0)); // 提取第一个（单个文本）结果
    }

    /**
     * 批量获取 embeddings（响应式版本）。
     *
     * @param texts 输入文本列表。
     * @return Mono<List < List < Double>>>：异步返回 embedding 向量列表。
     */
    public Mono<List<List<Double>>> getEmbeddings(List<String> texts) {
        if (texts.size() > 16) {
            return Mono.error(new IllegalArgumentException("Input texts size cannot exceed 16."));
        }

        // 构建请求体
        Map<String, Object> requestBody = Map.of(
                "model", "Baichuan-Text-Embedding",
                "input", texts
        );

        // 使用 WebClient 发送 POST 请求
        return webClient.post()
                .uri(bai_url)  // URI（完整 URL）
                .contentType(MediaType.APPLICATION_JSON)  // 等同于原 headers.setContentType
                .header("Authorization", "Bearer " + bai_key)  // 替换为你的 API Key 获取方式
                .bodyValue(requestBody)  // 请求体
                .retrieve()  // 执行请求
                .toEntity(String.class)  // 转换为 ResponseEntity<String>
                .onErrorMap(ex -> new RuntimeException("API call failed: " + ex.getMessage(), ex))  // 错误映射
                .map(response -> {  // 处理响应（在 map 中解析，避免阻塞）
                    if (response.getStatusCode().is2xxSuccessful()) {
                        try {
                            JsonNode root = objectMapper.readTree(response.getBody());
                            JsonNode dataArray = root.get("data");

                            List<List<Double>> embeddings = new ArrayList<>();
                            for (JsonNode embeddingNode : dataArray) {
                                JsonNode embeddingArray = embeddingNode.get("embedding");
                                List<Double> vector = new ArrayList<>();
                                for (JsonNode value : embeddingArray) {
                                    vector.add(value.asDouble());
                                }
                                embeddings.add(vector);
                            }
                            return embeddings;
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("API returned non-2xx status: " + response.getStatusCode());
                    }
                });
    }
    //---------------

    /**
     * 将单个文本转换为嵌入向量
     *
     * @param text 输入文本
     * @return 浮点数组向量（维度取决于模型，例如 ada-002 为 1536 维）
     */
    public List<Float> generateEmbedding(String text) {
        log.info("0 开始生成嵌入向量:{}", text);
        // 创建嵌入请求
        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .input(text)// 支持单个字符串或 List<String>
                .model("text-embedding-ada-002")// 推荐模型，维度 1536；或 "text-embedding-3-small" (维度 1536)
                .build();
        log.info("1 EmbeddingCreateParams:{}", JSON.toJSONString(params));

        try {
            CreateEmbeddingResponse createEmbeddingResponse = openAIClient.embeddings().create(params);
            log.info("2 CreateEmbeddingResponse:{}", JSON.toJSONString(createEmbeddingResponse));

            List<Embedding> embeddings = createEmbeddingResponse.data();
            log.info("3 List<Embedding>:{}", JSON.toJSONString(embeddings));

            if (!embeddings.isEmpty()) {
                Embedding embedding = embeddings.get(0);
                log.info("4 Embedding:{}", JSON.toJSONString(embedding));
                return embedding.embedding();
            }
        } catch (Exception e) {
            log.info("5 Embedding生成嵌入失败:{}", JSON.toJSONString(e.getMessage()));
        }
        return new ArrayList<>();  // 错误时返回空向量
    }


    /**
     * 批量生成嵌入（顺序对应输入 texts）
     *
     * @param texts 文本列表
     * @return List<List < Float>> 每个文本的向量，顺序一致
     * 当前 SDK 不支持批量 → 循环调用 generateEmbedding(text)（兼容方案）
     * <dependency>
     * <groupId>com.openai</groupId>
     * <artifactId>openai-client</artifactId>
     * <version>1.2.0</version>  <!-- 最新 -->
     * </dependency>
     */
    public List<List<Float>> generateBatchEmbeddingsA(List<String> texts) {
        log.info("0 开始批量生成嵌入向量: {}", JSON.toJSONString(texts));

        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .input(texts.toString())   //直接传入批量文本
                .model("text-embedding-ada-002")  // 或 "text-embedding-3-small"
                .build();

        log.info("1 EmbeddingCreateParams:{}", JSON.toJSONString(params));

        try {
            CreateEmbeddingResponse response = openAIClient.embeddings().create(params);
            log.info("2 CreateEmbeddingResponse:{}", JSON.toJSONString(response));

            List<Embedding> embeddings = response.data();
            log.info("3 批量 Embeddings: {}", JSON.toJSONString(embeddings));

            List<List<Float>> results = new ArrayList<>();
            for (Embedding embedding : embeddings) {
                results.add(embedding.embedding());
            }
            return results;

        } catch (Exception e) {
            log.error("批量生成嵌入失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 批量生成嵌入（示例：10 句文本）
     *
     * @param texts 文本列表
     * @return Map<String, float [ ]> 文本到向量的映射
     */
    public List<List<Float>> generateBatchEmbeddingsB(List<String> texts) {
        List<List<Float>> results = new ArrayList<>();
        for (String text : texts) {
            List<Float> vec = generateEmbedding(text);  // 单条方法
            results.add(vec);
        }
        return results;
    }
}
