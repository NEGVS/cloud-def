package xCloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/20 13:51
 * @ClassName WebClientConfig
 */
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                // 可选：配置基础URL、拦截器等
                // .baseUrl("https://api.example.com")
                .baseUrl("https://api.baichuan-ai.com") // 可选：设置基础 URL（如你的 embedding API）
                .defaultHeader("Content-Type", "application/json") // 可选：默认 Header
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 可选：处理大响应
                .build();
    }
}
