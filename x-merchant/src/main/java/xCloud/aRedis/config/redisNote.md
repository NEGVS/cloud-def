如果你想在 Redis 里既能存储字符串，又能存储对象（并且在 Redis 客户端看到的是 **JSON 格式**），推荐使用 `Jackson2JsonRedisSerializer`。

下面给你一个 **完整的 JSON 版 RedisTemplate 配置**：

---

### JSON 序列化配置类

```java
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key、hashKey 都使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // value、hashValue 使用 Jackson JSON 序列化
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        jacksonSerializer.setObjectMapper(objectMapper);

        // 配置序列化方式
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

---

### 使用效果

```java

@Autowired
private RedisTemplate<String, Object> redisTemplate;

public void test() {
    // 存字符串
    redisTemplate.opsForValue().set("test:string", "hello redis");

    // 存对象
    User user = new User(1L, "Andy");
    redisTemplate.opsForValue().set("test:user", user);
}
```

Redis 里会看到类似内容：

```
test:string -> "hello redis"

test:user -> {"id":1,"name":"Andy"}
```

---

这样就解决了：

1. key 都是纯字符串，Redis 客户端可读。
2. value 自动转 JSON，存对象也一目了然。

---

要不要我帮你写一个 **同时提供 StringRedisTemplate 和 JSON RedisTemplate** 的配置类？这样你既可以方便地存取纯字符串（轻量），又可以存取 JSON 对象（结构化）。
