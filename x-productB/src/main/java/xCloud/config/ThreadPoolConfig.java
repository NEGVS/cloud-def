package xCloud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

/**
 * @Description 全局线程池配置
 *              参数全部从 application.yml 动态读取，支持不同环境调优。
 *              使用 CallerRunsPolicy 拒绝策略：队列满时由调用方线程执行，
 *              既不丢任务，又能自动反压，适合日志写入等非关键异步场景。
 * @Author Andy Fan
 */
@Slf4j
@EnableAsync
@Configuration
public class ThreadPoolConfig {

    /** 核心线程数（默认 CPU 核心数 * 2，IO 密集型任务适用） */
    @Value("${thread-pool.core-size:#{T(java.lang.Runtime).getRuntime().availableProcessors() * 2}}")
    private int coreSize;

    /** 最大线程数 */
    @Value("${thread-pool.max-size:#{T(java.lang.Runtime).getRuntime().availableProcessors() * 4}}")
    private int maxSize;

    /** 空闲线程存活时间（秒） */
    @Value("${thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    /** 任务队列容量 */
    @Value("${thread-pool.queue-capacity:1000}")
    private int queueCapacity;

    /** 线程名前缀，便于日志追踪 */
    @Value("${thread-pool.thread-name-prefix:xCloud-async-}")
    private String threadNamePrefix;

    /**
     * 通用异步线程池
     * <p>
     * - LinkedBlockingQueue：有界队列，防止 OOM
     * - CallerRunsPolicy：队列满时调用方线程执行，实现反压，不丢任务
     * - allowCoreThreadTimeOut：核心线程也会在空闲后回收，节省资源
     */
    @Bean("taskExecutor")
    public ThreadPoolExecutor taskExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactory(threadNamePrefix),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        // 核心线程也参与超时回收，低峰期释放资源
        executor.allowCoreThreadTimeOut(true);

        log.info("线程池初始化完成 core={}, max={}, queue={}, prefix={}",
                coreSize, maxSize, queueCapacity, threadNamePrefix);
        return executor;
    }

    /** 自定义线程工厂，设置线程名和守护线程标志 */
    private static class ThreadFactory implements java.util.concurrent.ThreadFactory {
        private final String prefix;
        private final java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(1);

        ThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + counter.getAndIncrement());
            t.setDaemon(true); // 守护线程，JVM 退出时不阻塞
            return t;
        }
    }
}
