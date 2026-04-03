package xCloud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 全局线程池配置
 *              参数根据当前机器的 CPU 核心数和可用内存自动计算，无需手动配置。
 *              策略：IO 密集型任务（Embedding、数据库写入），core = CPU*2，max = CPU*4。
 *              队列容量根据可用堆内存动态估算，避免 OOM。
 *              拒绝策略：CallerRunsPolicy，队列满时由调用方线程执行，不丢任务 + 自动反压。
 * @Author Andy Fan
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    @Bean("taskExecutor")
    public ThreadPoolExecutor taskExecutor() {
        int cpus = Runtime.getRuntime().availableProcessors();

        // IO 密集型：核心线程 = CPU * 2，最大线程 = CPU * 4
        int coreSize = cpus * 2;
        int maxSize  = cpus * 4;

        // 队列容量：根据可用堆内存估算，每个任务预估 1KB，最小 512，最大 4096
        long availableMemoryMB = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        int queueCapacity = (int) Math.min(4096, Math.max(512, availableMemoryMB / 2));

        // 空闲线程存活 60s，核心线程同样参与回收
        int keepAliveSeconds = 60;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new NamedThreadFactory("xCloud-async-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.allowCoreThreadTimeOut(true);

        log.info("线程池自动初始化: cpu={}, core={}, max={}, queue={}",
                cpus, coreSize, maxSize, queueCapacity);
        return executor;
    }

    /** 自定义线程工厂：命名 + 守护线程 */
    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger counter = new AtomicInteger(1);

        NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
