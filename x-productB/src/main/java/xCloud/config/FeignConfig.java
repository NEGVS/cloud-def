package xCloud.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description 配置 Feign 的日志级别：
 * @Author Andy Fan
 * @Date 2025/9/8 16:44
 * @ClassName FeignConfig
 */
@Configuration
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
