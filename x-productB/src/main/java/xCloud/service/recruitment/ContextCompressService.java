package xCloud.service.recruitment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import xCloud.entity.recruitment.DocumentChunk;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 上下文压缩（Context Compression）
 *
 * 将多个召回文档块通过 LLM 压缩为与 query 最相关的精简上下文，
 * 减少 Prompt 长度，提升生成质量。
 */
@Slf4j
@Service
public class ContextCompressService {

    @Value("${ali.baseUrl}")
    private String baseUrl;

    @Value("${ali.api-key}")
    private String apiKey;

    @Value("${ali.chat_model_name}")
    private String model;

    private final WebClient webClient;

    public ContextCompressService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 压缩上下文：从多个文档块中提取与 query 最相关的句子
     *
     * @param query 用户问题
     * @param docs  候选文档块
     * @return 压缩后的上下文字符串
     */
    public String compress(String query, List<DocumentChunk> docs) {
        if (docs.isEmpty()) return "";

        String rawContext = docs.stream()
                .map(d -> "[来源: " + d.getSource() + "]\n" + d.getContent())
                .collect(Collectors.joining("\n\n---\n\n"));

        String systemPrompt = "你是一个信息提取助手。请从以下文档中提取与用户问题最相关的关键信息，" +
                "去除无关内容，保留原文表述，输出精简的上下文（不超过500字）。";

        String userPrompt = "用户问题：" + query + "\n\n文档内容：\n" + rawContext;

        try {
            Map<?, ?> response = webClient.post()
                    .uri(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of(
                            "model", model,
                            "messages", List.of(
                                    Map.of("role", "system", "content", systemPrompt),
                                    Map.of("role", "user", "content", userPrompt)
                            ),
                            "max_tokens", 600
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return rawContext;

            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.warn("上下文压缩失败，使用原始上下文: {}", e.getMessage());
            return rawContext;
        }
    }
}
