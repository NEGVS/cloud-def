package com.recruit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class LLMService {

    @Value("${llm.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String userInput, List<Map<String, Object>> jobs) {
        String prompt = buildPrompt(userInput, jobs);

        Map<String, Object> request = Map.of(
            "messages", List.of(Map.of("role", "user", "content", prompt)),
            "max_tokens", 500
        );

        Map<String, Object> response = restTemplate.postForObject(
            apiUrl, request, Map.class);

        return (String) ((Map) ((List) response.get("choices")).get(0))
            .get("message").get("content");
    }

    private String buildPrompt(String userInput, List<Map<String, Object>> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return userInput;
        }

        StringBuilder sb = new StringBuilder("用户问题: ").append(userInput)
            .append("\n\n匹配的职位:\n");

        for (int i = 0; i < jobs.size(); i++) {
            Map<String, Object> job = jobs.get(i);
            sb.append(i + 1).append(". ")
              .append(job.get("title")).append("\n")
              .append(job.get("desc")).append("\n\n");
        }

        sb.append("请根据以上职位信息回复用户。");
        return sb.toString();
    }
}
