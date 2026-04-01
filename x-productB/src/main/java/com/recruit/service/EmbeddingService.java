package com.recruit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class EmbeddingService {

    @Value("${embedding.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Float> generateEmbedding(String text) {
        Map<String, String> request = Map.of("input", text);
        Map<String, Object> response = restTemplate.postForObject(
            apiUrl, request, Map.class);
        return (List<Float>) ((Map) ((List) response.get("data")).get(0)).get("embedding");
    }
}
