package xCloud.openAiChatModel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class LLMStreamService {

    @Autowired
    private WebClient webClient;

    @Value("${llm.api.url}")
    private String llmApiUrl;

    public Flux<String> chatStream(String prompt) {
        return webClient.post()
            .uri(llmApiUrl)
            .bodyValue(Map.of(
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "stream", true
            ))
            .retrieve()
            .bodyToFlux(String.class);
    }
}
