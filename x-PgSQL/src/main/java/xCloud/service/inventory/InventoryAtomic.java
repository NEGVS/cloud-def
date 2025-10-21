package xCloud.service.inventory;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/21 09:42
 * @ClassName InventoryAtomic
 * 4. 使用 AtomicInteger：无锁并发（CAS 机制）
 * AtomicInteger 使用 CAS（Compare-And-Swap）原子操作，无需显式锁，高性能。适用于读多写少场景。
 * 示例：AtomicInteger 扣减库存
 * <p>
 * 优点：无锁，CPU 自旋重试高效；高可用下减少锁开销。
 * 物流应用：高 QPS 库存检查/扣减，结合数据库最终一致性。
 * 不可变对象整合：返回新 AtomicInteger 实例（immutable 风格）
 */
@Getter
public class InventoryAtomic {

    private AtomicInteger stock; // 不可变引用，但内部值可原子更新

    public InventoryAtomic(int initialStock) {
        this.stock = new AtomicInteger(initialStock);
    }

    /**
     * 无锁扣减，CAS循环
     *
     * @param qty q
     * @return boolean
     */
    public boolean deduct(int qty) {
        int current;
        int newStock;
        do {
            current = stock.get();//原子读取
            if (current < qty) {
                System.out.println("库存不足");
                return false;
            }
            newStock = current - qty;
        } while (!stock.compareAndSet(current, newStock));//原子更新， CAS失败重试
        System.out.println("扣减成功，剩余库存: " + newStock);
        return true;
    }

    public InventoryAtomic deductImmutable(int qty) {
        // 返回新对象, 保证线程安全,避免共享 mutable 状态
        return new InventoryAtomic(stock.get() - qty);

    }
}
