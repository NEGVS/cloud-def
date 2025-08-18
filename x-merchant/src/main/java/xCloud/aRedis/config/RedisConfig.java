package xCloud.aRedis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Description 写一个 Spring Boot 配置类，替换掉默认的 RedisTemplate，让 Redis 的 key/value 在 Redis 客户端里看起来都是 可读字符串。
 * <p>
 * 如果 全是字符串，我建议直接用 StringRedisTemplate，代码会更简洁。
 * <p>
 * 如果有对象要存，可以用 JSON 序列化（Jackson2JsonRedisSerializer），那样 Redis 里也能看到结构化的数据。
 * @Author Andy Fan
 * @Date 2025/8/18 11:26
 * @ClassName RedisConfig
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 设置 Key 的序列化方式为字符串
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置 Value 的序列化方式为字符串
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
