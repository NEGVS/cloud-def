package com.recruit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class ClusterService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String PYTHON_API = "http://localhost:5000/cluster";

    /**
     * 调用Python HDBSCAN聚类API
     */
    public Integer getJobCategory(String text) {
        Map<String, String> request = Map.of("text", text);
        Map<String, Object> response = restTemplate.postForObject(
            PYTHON_API, request, Map.class);
        return (Integer) response.get("category_id");
    }
}
