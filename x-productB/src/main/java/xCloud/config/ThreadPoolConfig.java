package xCloud.config;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.support.DynamicTp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 动态线程池配置（基于 dynamic-tp + Nacos）
 *              启动时根据 CPU 核心数自动计算初始参数；
 *              运行中可通过 Nacos 配置中心实时修改 core/max/queue 等参数，无需重启。
 *              监控指标（队列积压、活跃线程、拒绝次数）可在 dynamic-tp 控制台查看。
 * @Author Andy Fan
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 通用异步线程池
     * <p>
     * 加 @DynamicTp 注解后，dynamic-tp 会接管该线程池，
     * Nacos 中修改配置后自动热更新，无需重启服务。
     * <p>
     * 初始参数自动根据机器资源计算：
     * - coreSize  = CPU * 2（IO 密集型）
     * - maxSize   = CPU * 4
     * - queue     = 根据可用堆内存估算，范围 [512, 4096]
     */
    @Bean("taskExecutor")
    @DynamicTp("taskExecutor")
    public ThreadPoolExecutor taskExecutor() {
        int cpus = Runtime.getRuntime().availableProcessors();
        int coreSize = cpus * 2;
        int maxSize  = cpus * 4;
        long availableMemoryMB = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        int queueCapacity = (int) Math.min(4096, Math.max(512, availableMemoryMB / 2));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                coreSize,
                maxSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new NamedThreadFactory("xCloud-async-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.allowCoreThreadTimeOut(true);

        log.info("动态线程池初始化: cpu={}, core={}, max={}, queue={}",
                cpus, coreSize, maxSize, queueCapacity);
        return executor;
    }

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
