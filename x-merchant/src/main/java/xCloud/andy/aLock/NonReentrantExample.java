package xCloud.andy.aLock;

import java.util.concurrent.Semaphore;

/**
 * @Description 示例 3（对比）：不可重入的场景如何自锁死（以 Semaphore(1) 为例）
 *
 * Semaphore 不是锁，但常被拿来做互斥。它不是为“可重入”设计的：同一线程再次 acquire() 会把自己挂起，造成自我死锁（因为挂起后无法走到 release()）。
 * @Author Andy Fan
 * @Date 2025/8/31 18:25
 * @ClassName NonReentrantExample
 * 演示目的代码：运行会挂住（符合预期）。现实中不要这么写，这正是不可重入带来的典型坑
 */
public class NonReentrantExample {

    private final Semaphore semaphore = new Semaphore(1);

    public void outer() throws InterruptedException {
        semaphore.acquire();
        try {
            System.out.println(thread() + "enter outer");
            inner();//将尝试再次acquire，因为非重入锁，将阻塞，永远到不了release
            System.out.println(thread() + "这行永远不会打印");
        } finally {
            semaphore.release();
        }
    }

    public void inner() throws InterruptedException {
        System.out.println(thread() + "enter inner 尝试获取信号量，将阻塞（自锁死）");
        semaphore.acquire();// 获取信号量
        try {
            System.out.println(thread() + " 不可达");
        } finally {
            semaphore.release();
        }
    }

    private static String thread() {
        return "[" + Thread.currentThread().getName() + "]";
    }

    public static void main(String[] args) {

        NonReentrantExample demo = new NonReentrantExample();
        new Thread(() -> {
            try {
                demo.outer();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "AndyThread").start();

    }
}
