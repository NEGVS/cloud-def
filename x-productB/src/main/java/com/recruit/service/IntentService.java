package com.recruit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class IntentService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String PYTHON_API = "http://localhost:5000/intent";

    /**
     * 调用Python意图识别API
     */
    public boolean isJobIntent(String text) {
        Map<String, String> request = Map.of("text", text);
        Map<String, Object> response = restTemplate.postForObject(
            PYTHON_API, request, Map.class);
        return "job".equals(response.get("intent"));
    }
}
