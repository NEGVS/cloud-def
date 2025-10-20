package xCloud.service;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/20 20:00
 * @ClassName DistributedStockService
 * 分布式高可用库存扣减方案，结合 分布式锁 + 原子操作 + Kafka 异步消息，我给你整理一个完整设计与代码示例：
 * <p>
 * 🧩 分布式库存扣减高可用方案
 * 1️⃣ 核心思路
 * <p>
 * 分布式锁（Redis/Redisson）
 * <p>
 * 保证同一时间只有一个服务实例对同一商品库存进行扣减，防止超卖。
 * <p>
 * 原子操作（DB/Redis）
 * <p>
 * Redis 原子递减库存，DB 乐观锁保证最终一致性。
 * <p>
 * Kafka 异步处理订单
 * <p>
 * 扣减库存成功后，发送消息到 Kafka 队列，由消费者异步创建订单，降低请求压力。
 * <p>
 * 2️⃣ 流程设计
 * 用户下单请求 →
 * 1. 获取 Redis 分布式锁（Lock） →
 * 2. 查询 Redis 库存或 DB 库存 →
 * 3. 判断库存是否足够 →
 * 4. 扣减库存（Redis decrBy + DB乐观锁） →
 * 5. 发送 Kafka 消息 → 异步创建订单
 * 6. 释放 Redis 分布式锁
 * 7. 异步消费者确认订单，失败回滚库存
 * <p>
 * <p>
 * 特点：
 * <p>
 * 锁粒度小：按商品 ID 上锁
 * <p>
 * 操作快速：Redis 原子扣减，减少 DB 竞争
 * <p>
 * 异步下单：Kafka 异步消费，削峰填谷
 * <p>
 * 高可用：锁、原子操作、消息队列结合保证一致性
 * <p>
 * 高可用优化建议
 * <p>
 * 分布式锁
 * <p>
 * 使用 Redisson RedLock 保证多 Redis 节点安全
 * <p>
 * 设置合理过期时间，避免死锁
 * <p>
 * 库存缓存
 * <p>
 * 热点商品库存先缓存到 Redis
 * <p>
 * 定时同步 DB 避免数据漂移
 * <p>
 * 异步削峰
 * <p>
 * Kafka + 消费端限流，避免订单系统瞬时压力过大
 * <p>
 * 幂等设计
 * <p>
 * 消费端处理重复消息不会重复扣减库存或创建订单
 * <p>
 * 监控告警
 * <p>
 * 锁超时、消息积压、库存异常报警
 */
@Slf4j
@Service
public class DistributedStockService {
    @Autowired
    private RedissonClient redisson;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 扣减库存并异步下单
     *
     * @param productId 商品ID
     * @param num       扣减数量
     * @param orderId   订单ID
     * @return 是否扣减成功
     */
    public boolean deductStock(String productId, int num, String orderId) {
        RLock lock = redisson.getLock("lock:stock:" + productId);
        try {
            if (lock.tryLock(500, 2000, TimeUnit.MILLISECONDS)) {

                // 1.查询库存
                Integer stock = Integer.parseInt(redisTemplate.opsForValue().get("stock:" + productId));
                if (stock < num) {
                    log.warn("库存不足, productId={}, stock={}, required={}", productId, stock, num);
                    return false;
                }

                // 2.扣减库存,扣减 Redis 库存（原子操作）
                Long decrement = redisTemplate.opsForValue().decrement("stock:" + productId, num);

                //3.更新db库存（乐观锁防止超卖）
                int update = jdbcTemplate.update("UPDATE product_stock SET stock = stock - ? WHERE product_id = ? AND stock >= ?",
                        num, productId, num);
                if (update <= 0) {
                    log.error("DB 扣减库存失败, productId={}, num={}", productId, num);
                    // 回滚 Redis 扣减库存
                    redisTemplate.opsForValue().increment("stock:" + productId, num);
                    return false;
                }
                // 4.发送 Kafka 订单消息
                Map<String, Object> orderMsg = new HashMap<>();
                orderMsg.put("orderId", orderId);
                orderMsg.put("productId", productId);
                orderMsg.put("num", num);
                CompletableFuture<SendResult<String, String>> send = kafkaTemplate.send("andy-order-topic", JSONUtil.toJsonStr(orderMsg));
                log.info("库存扣减成功, productId={}, num={}, orderId={}", productId, num, orderId);
                return true;
            } else {
                log.warn("未获取到锁, productId={}", productId);
                // 锁未获取成功, 返回失败
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
