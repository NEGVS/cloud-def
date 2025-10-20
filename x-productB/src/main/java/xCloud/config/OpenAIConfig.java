package xCloud.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/17 10:54
 * @ClassName OpenAIConfig
 */
@Configuration
public class OpenAIConfig {
    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Bean
    public OpenAIClient openAIClient() {
        return OpenAIOkHttpClient.builder()
                .apiKey(openaiApiKey)
                .build();
    }

}
