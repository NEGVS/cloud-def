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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    @Value("${openai.apiUrl}")
    private String apiUrl;
    @Value("${openai.apiKey}")
    private String apiKey;
    @Value("${vector.dim}")
    private int dim;

    @Resource
    private OpenAIClient openAIClient;

    @Resource
    private RestTemplate restTemplate;

    /**
     * b1 【百川】将单个文本转换为向量（embedding）
     *
     *
     * @param text 输入文本，长度不超过512 token。
     * @return 1024维的embedding向量列表。
     * @throws RuntimeException 如果API调用失败或响应无效。
     */
    public List<Double> getEmbedding(String text) {
        return getEmbeddings(List.of(text)).get(0);
    }

    /**
     * b2 【百川】将多个文本转换为向量（embedding），批量最多16个。
     *
     * @param texts 输入文本列表，每个不超过512 token，列表大小不超过16。
     * @return embedding向量列表，对应输入顺序。
     * @throws RuntimeException 如果API调用失败或响应无效。
     */
    public List<List<Double>> getEmbeddings(List<String> texts) {
        if (texts.size() > 16) {
            throw new IllegalArgumentException("Input texts size cannot exceed 16.");
        }

        // 构建请求体
        Map<String, Object> requestBody = Map.of(
                "model", "Baichuan-Text-Embedding",
                "input", texts
        );

        // 设置Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 发送POST请求
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("API call failed with status: " + response.getStatusCode());
            }
            ObjectMapper objectMapper = new ObjectMapper();
            // 解析响应
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
            throw new RuntimeException("Failed to get embeddings: " + e.getMessage(), e);
        }
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
