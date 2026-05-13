package xCloud.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 基础 Bean 配置：WebClient + ObjectMapper
 */
@Configuration
public class WebClientConfig {

    /**
     * 通用 WebClient，不设置 baseUrl，各调用方自行拼接完整 URL。
     * 原 baseUrl 写死百川地址会导致阿里/DeepSeek 等其他 API 调用出错。
     * 响应体缓冲上限设为 10MB，兼容大文档检索返回。
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    /**
     * 全局 ObjectMapper：
     * - 注册 JavaTimeModule，支持 LocalDateTime 序列化
     * - 忽略未知字段，兼容 LLM 返回的额外字段
     * - 禁用日期转时间戳，输出 ISO 格式字符串
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
