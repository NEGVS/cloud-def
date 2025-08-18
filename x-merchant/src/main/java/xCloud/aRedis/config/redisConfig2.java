//package xCloud.aRedis.config;
//
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.PropertyAccessor;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
///**
// * @Description 如果你想在 Redis 里既能存储字符串，又能存储对象（并且在 Redis 客户端看到的是 JSON 格式），推荐使用 Jackson2JsonRedisSerializer。
// * @Author Andy Fan
// * @Date 2025/8/18 11:30
// * @ClassName redisConfig2
// */
//public class redisConfig2 {
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//
//        // key、hashKey 都使用 String 序列化
//        StringRedisSerializer stringSerializer = new StringRedisSerializer();
//
//        // value、hashValue 使用 Jackson JSON 序列化
//        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        objectMapper.activateDefaultTyping(
//                objectMapper.getPolymorphicTypeValidator(),
//                ObjectMapper.DefaultTyping.NON_FINAL
//        );
//        jacksonSerializer.setObjectMapper(objectMapper);
//
//        // 配置序列化方式
//        template.setKeySerializer(stringSerializer);
//        template.setHashKeySerializer(stringSerializer);
//        template.setValueSerializer(jacksonSerializer);
//        template.setHashValueSerializer(jacksonSerializer);
//
//        template.afterPropertiesSet();
//        return template;
//    }
//}
