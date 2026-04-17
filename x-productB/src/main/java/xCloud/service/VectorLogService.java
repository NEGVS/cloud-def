package xCloud.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xCloud.entity.TextVectorLog;
import xCloud.mapper.TextVectorLogMapper;
import xCloud.tools.CodeX;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 向量日志公共写入服务
 * <p>
 * 设计原则：
 * - 调用方完全异步，offer 入队即返回，不阻塞业务主流程
 * - 内部单消费线程按批次（BATCH_SIZE=200）或超时（DRAIN_TIMEOUT_MS=500ms）触发批量 insert
 * - 队列容量上限 QUEUE_CAPACITY=50000，超限仅打告警日志，不抛异常
 * - @PreDestroy 优雅关闭：排空队列后再停止消费线程，保证数据不丢失
 * <p>
 * 使用方式（任意 Service 注入后直接调用）：
 * <pre>
 *   vectorLogService.asyncLog(id, text, vectorJson, source, remark);
 *   vectorLogService.asyncLogBatch(logs);  // 已构建好 TextVectorLog 列表
 * </pre>
 *
 * @Author Andy Fan
 */
@Slf4j
@Service
public class VectorLogService {

    /**
     * 每批最多写入条数，兼顾 MySQL 单 SQL 包大小与吞吐
     */
    private static final int BATCH_SIZE = 200;

    /**
     * 超过此时间（ms）即使未满批也强制刷写，避免低流量时日志积压
     */
    private static final long DRAIN_TIMEOUT_MS = 500L;

    /**
     * 队列上限；超限直接丢弃并告警，保护内存
     */
    private static final int QUEUE_CAPACITY = 50_000;

    @Resource
    private TextVectorLogMapper textVectorLogMapper;

    /**
     * 无界阻塞队列（有界由 QUEUE_CAPACITY 控制），生产者 offer，消费者 poll
     */
    private final LinkedBlockingQueue<TextVectorLog> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    /**
     * 后台消费线程（单线程，避免并发写 MySQL 产生锁竞争）
     */
    private volatile boolean running = true;
    private Thread consumerThread;

    /* ─── 生命周期 ───────────────────────────────────────────────── */

    /**
     * 应用启动后自动启动消费线程（虚拟线程，极低内存占用）
     */
    @PostConstruct
    public void start() {
        consumerThread = Thread.ofVirtual()
                .name("vector-log-consumer")
                .start(this::consume);
        log.info("VectorLogService 消费线程已启动");
    }

    /**
     * 应用关闭时优雅停止：先标记停止，再排空队列，最后中断线程
     */
    @PreDestroy
    public void stop() {
        running = false;
        // 唤醒可能阻塞在 poll 的消费线程
        consumerThread.interrupt();
        try {
            // 等待消费线程最多 5s 完成剩余数据写入
            consumerThread.join(5_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // 最终 flush：防止 join 超时后仍有残余数据
        flushRemaining();
        log.info("VectorLogService 消费线程已停止，队列已排空");
    }

    /* ─── 公共 API ───────────────────────────────────────────────── */

    /**
     * 异步写入单条向量日志（非阻塞）
     *
     * @param id     与 Milvus 主键保持一致的雪花 ID
     * @param text   原始文本内容
     * @param vector 向量 JSON 字符串（由调用方序列化）
     * @param source 来源标识，如文件名、业务模块名
     * @param remark 备注信息（可为 null）
     */
    public void asyncLog(Long id, String text, String vector, String source, String remark) {
        TextVectorLog log = buildLog(id, text, vector, source, remark);
        offerToQueue(log);
    }

    /**
     * 异步批量写入向量日志（非阻塞，逐条 offer 入队）
     * <p>
     * 适用于已构建好 TextVectorLog 列表的场景（如 PdfChunkService.embedBatch 回调）
     *
     * @param logs 待写入的向量日志列表
     */
    public void asyncLogBatch(List<TextVectorLog> logs) {
        if (logs == null || logs.isEmpty()) return;
        for (TextVectorLog log : logs) {
            offerToQueue(log);
        }
    }

    /**
     * 构建 TextVectorLog 实体的便捷工厂方法
     *
     * @param id     主键（与 Milvus id 对应）
     * @param text   原始文本
     * @param vector 向量序列化字符串
     * @param source 来源
     * @param remark 备注
     * @return 已填充字段的 TextVectorLog 实体
     */
    public static TextVectorLog buildLog(Long id, String text, String vector,
                                         String source, String remark) {
        TextVectorLog entity = new TextVectorLog();
        entity.setId(id != null ? id : CodeX.nextId());
        entity.setText(text);
        entity.setVector(vector);
        entity.setCreate_time(LocalDateTime.now());
        entity.setSource(source);
        entity.setRemark(remark);
        return entity;
    }

    /* ─── 内部实现 ───────────────────────────────────────────────── */

    /**
     * 将日志对象 offer 入队，超限时仅告警不阻塞
     */
    private void offerToQueue(TextVectorLog entry) {
        boolean offered = queue.offer(entry);
        if (!offered) {
            log.warn("VectorLogService 队列已满（容量={}），丢弃一条日志，source={}",
                    QUEUE_CAPACITY, entry.getSource());
        }
    }

    /**
     * 消费循环：从队列中攒批，满 BATCH_SIZE 或等待超 DRAIN_TIMEOUT_MS 则触发一次写库
     */
    private void consume() {
        List<TextVectorLog> buffer = new ArrayList<>(BATCH_SIZE);
        while (running || !queue.isEmpty()) {
            try {
                // 阻塞等待第一条，超时则直接刷写（避免低流量时数据长期积压）
                TextVectorLog first = queue.poll(DRAIN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (first != null) {
                    buffer.add(first);
                    // 非阻塞地继续排干队列，直到凑满一批
                    queue.drainTo(buffer, BATCH_SIZE - 1);
                }

                if (!buffer.isEmpty()) {
                    flush(buffer);
                    buffer.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // 被 stop() 唤醒，退出循环前 flush 剩余
                break;
            } catch (Exception e) {
                log.error("VectorLogService 消费异常: {}", e.getMessage(), e);
                buffer.clear(); // 避免重复写入脏数据
            }
        }
    }

    /**
     * 批量写入 MySQL，异常时降级为逐条写入（保证最大数据完整性）
     *
     * @param batch 待写入批次
     */
    private void flush(List<TextVectorLog> batch) {
        try {
            textVectorLogMapper.insertBatch(batch);
            log.debug("VectorLogService 批量写入 {} 条", batch.size());
        } catch (Exception e) {
            log.error("VectorLogService 批量写入失败，降级逐条写入，size={}: {}",
                    batch.size(), e.getMessage());
            // 降级：逐条写，最大程度保住数据
            for (TextVectorLog item : batch) {
                try {
                    textVectorLogMapper.insert(item);
                } catch (Exception ex) {
                    log.error("VectorLogService 逐条写入也失败，id={}: {}", item.getId(), ex.getMessage());
                }
            }
        }
    }

    /**
     * 应用关闭时排空队列中的剩余数据
     */
    private void flushRemaining() {
        List<TextVectorLog> remaining = new ArrayList<>();
        queue.drainTo(remaining);
        if (!remaining.isEmpty()) {
            log.info("VectorLogService 关闭时排空剩余 {} 条", remaining.size());
            // 按批次写入
            for (int i = 0; i < remaining.size(); i += BATCH_SIZE) {
                List<TextVectorLog> batch = remaining.subList(i,
                        Math.min(i + BATCH_SIZE, remaining.size()));
                flush(batch);
            }
        }
    }
}
