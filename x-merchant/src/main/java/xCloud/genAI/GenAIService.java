package xCloud.genAI;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/7/2 16:42
 * @ClassName GenAIService 创建一个服务类，负责向 Flask 的 /generate-content 端点发送 POST 请求并处理响应。
 */
@Service
public class GenAIService {
    private final RestTemplate restTemplate;

    private final String flaskUrl = "http://localhost:8085/generate-content";

    public GenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateContent(String prompt) {
//        set header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

//        construct request body
        String requestBody = "{\"prompt\": \"" + prompt + "\"}";
        // Encapsulation request,
        HttpEntity<String> stringHttpEntity = new HttpEntity<>(requestBody, httpHeaders);
//send request
        try {
            return restTemplate.postForObject(flaskUrl, stringHttpEntity, String.class);
        } catch (Exception e) {
            return "{\"error\":\"调用 Flask API 失败: " + e.getMessage() + "\"}";
        }
    }
}
