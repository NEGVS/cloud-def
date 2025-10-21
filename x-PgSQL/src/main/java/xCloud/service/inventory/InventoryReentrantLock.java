package xCloud.service.inventory;

import lombok.Getter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/21 09:29
 * @ClassName InventoryReentrantLock
 * 3. 使用 JUC 工具：ReentrantLock 保护库存扣减
 * ReentrantLock 更灵活：支持公平/非公平锁、tryLock（超时避免死锁）、中断。适用于中等并发。
 * 示例：用 ReentrantLock 保护扣减
 */
@Getter
public class InventoryReentrantLock {

    private int stock;

    private final ReentrantLock lock = new ReentrantLock(); // 可重入锁

    public InventoryReentrantLock(int initialStock) {
        this.stock = initialStock;
    }

    // 用 Lock 保护扣减（物流核心）
    public boolean deduct(int qty) {
        if (lock.tryLock()) { // tryLock() 高可用：非阻塞，失败不等待
            try {
                if (stock >= qty) {
                    stock -= qty;
                    System.out.println("扣减成功，剩余库存: " + stock);
                    return true;
                }
                return false;
            } finally {
                lock.unlock(); // 必须 finally 释放，避免死锁
            }
        } else {
            // 高可用：超时重试或降级（e.g., 抛异常或队列补偿）
            System.out.println("获取锁失败，重试或补偿");
            return false;
        }
    }

    // 支持超时：tryLock(1, TimeUnit.SECONDS)
    public boolean deductWithTimeout(int qty, long timeout, TimeUnit unit) {
        try {
            if (lock.tryLock(timeout, unit)) {
                try {
                    if (stock >= qty) {
                        stock -= qty;
                        return true;
                    }
                    return false;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断
            return false;
        }
        return false;
    }



}
