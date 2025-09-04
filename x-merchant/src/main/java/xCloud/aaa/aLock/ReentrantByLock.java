package xCloud.aaa.aLock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description 示例 2：ReentrantLock 的可重入与持有计数（getHoldCount）
 * @Author Andy Fan
 * @Date 2025/8/31 17:15
 * @ClassName ReentrantByLock
 * 同一线程可重复 lock()，getHoldCount() 会显示当前线程持有次数。
 *
 * 必须成对 unlock()：获取几次就要释放几次，直至计数归零，锁才真正释放。
 *
 * 适合需要：可中断获取（lockInterruptibly()）、超时尝试（tryLock(timeout, unit)）、条件队列（newCondition()）等高级控制。
 */
public class ReentrantByLock {

    private final ReentrantLock lock = new ReentrantLock();//default Fairness=false,reentrant=true

    //new ReentrantLock(true),//fair lock,fifo,more stable,more slow
    public void outer() {
        lock.lock();
        try {
            System.out.println(thread() + "enter outer()...,holdCount = " + lock.getHoldCount());
            inner();
            System.out.println(thread() + "leave outer()...,holdCount = " + lock.getHoldCount());
        } finally {
            lock.unlock(); // outer`s unlock
        }
    }

    public void inner() {
        lock.lock();
        try {
            System.out.println(thread() + ":enter inner(),holdCount = " + lock.getHoldCount());
            //reentrant
            lock.lock();
            try {
                System.out.println(thread() + ":enter inner(),holdCount = " + lock.getHoldCount());
            } finally {
                lock.unlock();    // the reentrant lock
                System.out.println(thread() + ":unlock inner(),holdCount = " + lock.getHoldCount());

            }
        } finally {
            lock.unlock(); //the inner lock
            System.out.println(thread() + ":unlock inner(),holdCount = " + lock.getHoldCount());

        }
    }

    private static String thread() {
        return Thread.currentThread().getName();
    }

    public static void main(String[] args) {
        ReentrantByLock reentrantByLock = new ReentrantByLock();
        Thread t1 = new Thread(reentrantByLock::outer, "T1");
        t1.setName("Andy ");
        t1.start();

    }
}
