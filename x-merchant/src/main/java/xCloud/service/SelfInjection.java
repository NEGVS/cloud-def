package xCloud.service;

import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/4 17:25
 * @ClassName SelfInjection
 */
@Component
@CacheConfig(cacheNames = "selfInjectionAddOne")
public class SelfInjection {
    // 使用 Lazy 注解，自注入
    @Lazy
    @Resource
    private SelfInjection selfInjection;

    private int counter = 0;

    @Cacheable
    public int addOne(int n) {
        counter++;
        return n + 1;
    }

    public int addOneAndDouble(int n) {
        return selfInjection.addOne(n) + selfInjection.addOne(n);
    }

    @CacheEvict(allEntries = true)
    public void resetCache() {
        counter = 0;
    }
}
