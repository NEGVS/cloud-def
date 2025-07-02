package xCloud.genAI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/7/2 16:44
 * @ClassName AppConfig 3. 配置 RestTemplate
 * 在 Spring Boot 应用中配置 RestTemplate Bean：
 */
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
