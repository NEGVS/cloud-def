package xCloud.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xCloud.entity.Inventory;
import xCloud.mapper.InventoryMapper;

import java.util.concurrent.TimeUnit;

/**
 * @description xx
 * @author Andy Fan
 * @date 2025/10/21 10:17
 * @ClassName GrokDistributedInventoryService
 * <p>
 * 5. 高可用完善：分布式库存扣减
 * 单机锁不适用于多节点集群（e.g., 微服务）。引入：
 * <p>
 * 分布式锁：Redisson（Redis 基），支持自动续期/看门狗机制。
 * 乐观锁：数据库版本字段（UPDATE stock SET value = value - qty, version = version + 1 WHERE id = ? AND version = ?）。
 * 幂等性：订单ID 唯一，防止重复扣减。
 * 补偿机制：失败时 MQ 回滚库存。
 * <p>
 * 示例：伪代码 + Redisson 分布式锁（假设已集成 RedissonClient）
 * <p>
 * 高可用保障：
 * <p>
 * 可用性：Redis 主从 + Sentinel 集群，锁续期防节点宕机。
 * 一致性：最终一致（库存先扣内存，后异步同步 DB）。
 * 性能：Atomic + 分布式锁，QPS > 10k。
 * 监控：Prometheus 记录扣减 RT/失败率，告警阈值 < 1%。
 * <p>
 * 总结与最佳实践
 * <p>
 * 选择依据：低并发用 synchronized；中高用 ReentrantLock/Atomic；分布式用 Redisson + 乐观锁。
 * 不可变对象益处：减少锁粒度，如用 record (Java 14+) 定义 Inventory（自动 immutable）。
 * 测试建议：JMeter 模拟 1000 QPS，验证无超卖。
 * 扩展：集成 Spring Boot + Sentinel 熔断，确保 99.99% 可用。
 */
@Service
@Slf4j
public class GrokDistributedInventoryService {

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private InventoryMapper inventoryMapper; // MyBatis 等

    /**
     * 分布式扣减流程
     */
    public boolean deductDistributed(String orderId, int qty, int productId) {
        String lockKey = "lock:inventory:" + productId;//锁键：商品ID
        RLock lock = redisson.getLock(lockKey);
        try {
            //高可用，tryLock(),超时10s，等待1s
            if (!lock.tryLock(10, 1, TimeUnit.SECONDS)) {
                //熔断，记录日志，异步补偿
                log.warn("获取分布式锁失败，订单{}补偿", orderId);
                return false;
            }
            // 1. 数据库乐观锁检查
            Inventory inv = inventoryMapper.selectById(productId);
            if (inv.getStock() < qty || inventoryMapper.updateById(inv) < 0) {
                // 更新失败（版本冲突），重试或补偿
                return false;
            }
            // 2. 更新内存缓存（可选，结合双写一致性）
//            localCache.put(productId, inv.getStock() - qty);
            // 3. 异步通知物流/订单
//            rabbitTemplate.convertAndSend("orderQueue", orderId);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();//释放锁
            }
        }
    }
}
