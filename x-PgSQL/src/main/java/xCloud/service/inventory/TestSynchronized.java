package xCloud.service.inventory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/21 09:36
 * @ClassName TestSynchronized
 */
public class TestSynchronized {
    public static void main(String[] args) {
        InventorySynchronized inventory = new InventorySynchronized(10);
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                boolean deduct = inventory.deduct(2);
                System.out.println("--------------线程" + Thread.currentThread().getName() + "开始执行");

                System.out.println("线程1：" + (deduct ? "扣减成功" : "扣减失败"));
            }
        });
        t1.start();
        // 模拟 20 个线程并发扣减 1 个
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                System.out.println("线程 " + Thread.currentThread().getName() + " 开始执行");
                boolean deduct = inventory.deduct(1);
            }).start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AtomicInteger stock; // 不可变引用，但内部值可原子更新

        int a = 13;
        int b = 123;
        AtomicInteger aa = new AtomicInteger(13);
        AtomicInteger bb = new AtomicInteger(14);
        System.out.println(aa.compareAndSet(a, b));
        //if aa = 期望值 a , 则更新为 b，return true; 否则不更新,return false;
        System.out.println(aa.get());

        System.out.println(bb.get());
        System.out.println(a);
        System.out.println(b);


    }
}
