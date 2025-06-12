package xCloud.service.selfInjection;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/4 16:06
 * @ClassName AddComponent
 */
@Component
@CacheConfig(cacheNames = "addOne")
public class AddComponent {
    private int counter = 0;

    @Cacheable
    public int addOne(int n) {
        counter++;
        return n + 1;
    }

    @CacheEvict
    public void resetCache() {
        counter = 0;
    }

    public int addOneAndDouble(int n) {
        return this.addOne(n) + this.addOne(n);
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
