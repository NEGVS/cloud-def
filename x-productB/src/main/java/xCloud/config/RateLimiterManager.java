package xCloud.config;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 限流器管理类
 * 使用Google Guava的RateLimiter实现令牌桶算法
 *
 * @author Andy Fan
 * @date 2026/08/27
 */
@Slf4j
@Component
public class RateLimiterManager {

    /**
     * 全局限流器（所有用户共享）
     * 每秒允许10个请求
     */
    private final RateLimiter globalRateLimiter = RateLimiter.create(10.0);

    /**
     * 用户级别限流器（每个IP或用户独立）
     * key: IP地址或用户ID
     * value: 该用户的限流器（每秒允许2个请求）
     */
    private final ConcurrentHashMap<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();

    /**
     * 每个用户的限流速率（每秒允许的请求数）
     */
    private static final double USER_PERMITS_PER_SECOND = 2.0;

    /**
     * 全局限流检查
     *
     * @param timeout 等待超时时间（毫秒）
     * @return true-通过限流，false-被限流
     */
    public boolean tryAcquireGlobal(long timeout) {
        boolean acquired = globalRateLimiter.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        if (!acquired) {
            log.warn("🚫 [全局限流] 请求被限流，当前速率已达上限");
        }
        return acquired;
    }

    /**
     * 用户级限流检查
     *
     * @param userId  用户标识（IP地址或用户ID）
     * @param timeout 等待超时时间（毫秒）
     * @return true-通过限流，false-被限流
     */
    public boolean tryAcquireUser(String userId, long timeout) {
        RateLimiter rateLimiter = userRateLimiters.computeIfAbsent(userId,
                k -> RateLimiter.create(USER_PERMITS_PER_SECOND));

        boolean acquired = rateLimiter.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        if (!acquired) {
            log.warn("🚫 [用户限流] 用户 {} 请求被限流，超过每秒{}次限制", userId, USER_PERMITS_PER_SECOND);
        }
        return acquired;
    }

    /**
     * 组合限流检查（全局 + 用户）
     *
     * @param userId 用户标识
     * @return true-通过限流，false-被限流
     */
    public boolean tryAcquire(String userId) {
        // 先检查全局限流（不等待，立即返回）
        if (!tryAcquireGlobal(0)) {
            return false;
        }

        // 再检查用户限流（等待500ms）
        return tryAcquireUser(userId, 500);
    }

    /**
     * 清理长期未使用的用户限流器（防止内存泄漏）
     * 建议定时任务每小时执行一次
     */
    public void cleanupInactiveUserLimiters() {
        int beforeSize = userRateLimiters.size();
        // 这里简单实现，生产环境可以记录最后访问时间
        if (beforeSize > 1000) {
            userRateLimiters.clear();
            log.info("🧹 [限流器清理] 清理了 {} 个用户限流器", beforeSize);
        }
    }

    /**
     * 获取统计信息
     */
    public String getStats() {
        return String.format("全局限流器速率: %.1f/s, 用户限流器数量: %d",
                globalRateLimiter.getRate(), userRateLimiters.size());
    }
}
