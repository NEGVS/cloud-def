package xCloud.service.inventory;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/20 20:48
 * @ClassName InventorySynchronized
 * 在物流系统中，库存扣减是高并发场景的核心操作（如电商下单、仓库出货）。多线程/多进程同时扣减可能导致超卖（库存负数）或库存不一致问题。为确保线程安全，
 * 我们可以使用 synchronized 块/方法控制共享资源、java.util.concurrent (JUC) 工具（如 ReentrantLock、AtomicInteger）实现更灵活的锁机制，以及 不可变对象 减少共享状态的竞争。
 * 同时，为实现 高可用（High Availability），
 * 需考虑分布式环境：引入分布式锁（如基于 Redis 的 Redisson）、乐观锁（数据库版本控制）、事务回滚、熔断/重试机制，避免单点故障和死锁。
 *
 * 1. 基本流程概述
 * 物流库存扣减的完整流程（伪代码）：
 *
 * 接收请求：订单服务接收扣减请求（包含商品ID、扣减数量）。
 * 加锁检查：获取锁，原子性检查库存 ≥ 扣减量。
 * 扣减库存：更新内存/数据库库存（使用事务）。
 * 更新下游：通知订单服务、物流服务（异步消息队列如 Kafka）。
 * 释放锁：确保 finally 块释放。
 * 异常处理：超时/失败时回滚，重试或补偿（高可用关键）。
 * 监控告警：记录扣减日志，监控库存阈值。
 *
 * 高可用扩展：
 *
 * 单机高可用：使用 JUC 工具 + 不可变对象。
 * 分布式高可用：Redis 分布式锁 + 数据库乐观锁 + 幂等性（订单唯一ID）。
 * 容错：锁超时（tryLock）、熔断（Hystrix/Sentinel）、服务降级。
 *
 * 下面逐步提供示例代码（Java），从简单到高级。假设库存存储在内存中（实际结合数据库）。
 * <p>
 *
 *     2 使用 synchronized 控制共享资源
 *     synchronized 是 JVM 内置锁，简单但不可中断、不可超时。适用于低并发单机场景。
 *     示例：synchronized 方法扣减库存
 * 优点：简单，自动处理可见性/有序性。
 * 缺点：不可中断，高并发下性能差（所有线程竞争同一锁）。
 * 物流应用：单仓库低并发扣减。
 */
@Getter
@Slf4j
public class InventorySynchronized {

    private int stock; // 共享资源

    public InventorySynchronized(int initialStock) {
        this.stock = initialStock;
    }

    private final Object lockObj = new Object(); // 专用锁对象，避免锁竞争

    public void incrementCount() {
        synchronized (lockObj) { // 块级锁，仅锁关键代码
//            count++; // 共享变量
        }
        // 其他非关键代码不受锁影响
    }

    public synchronized boolean deduct(int qty) {
        if (stock >= qty) {
            stock -= qty;//原子操作
            log.info("库存扣减成功，剩余库存：{}", stock);
            return true;
        }
        log.warn("库存不足，扣减失败");
        return false;
    }


}
