package xCloud.aaa.aLock;

/**
 * @Description 可重入（Reentrant）指的是：同一线程在已经持有某个锁的情况下，可以再次获取同一把锁而不会被阻塞。
 * 它会在内部为每个线程维护一个持有计数（hold count）：同一线程每获取一次锁，计数 +1；每 unlock()/离开同步块一次，计数 −1；直到计数归零，锁才真正释放给其他线程。
 * <p>
 * Java 中：
 * <p>
 * synchronized 的内置监视器锁是可重入的。
 * <p>
 * java.util.concurrent.locks.ReentrantLock 也是可重入的（名字就叫“可重入锁”）。
 * <p>
 * 为什么需要可重入？
 * 为了支持递归调用和同一线程的嵌套调用而不产生自我死锁。如果锁不可重入，线程在方法 A 获得锁后，调用方法 B（还要同一把锁）会卡死自己。
 * @Author Andy Fan
 * @Date 2025/8/31 17:04
 * @ClassName ReentrantBySynchronized
 */
public class ReentrantBySynchronized {

    public synchronized void outer() {
        System.out.println(thread() + "enter outer()...");
        // the same thread enter inner(),not block
        inner();
        System.out.println(thread() + "====leave outer()...");

    }

    public synchronized void inner() {
        System.out.println(thread() + "enter inner()...");
        //recursive  is not problem , because synchronized is reentrant, so it can be called recursively
        recursive(3);
        System.out.println(thread() + "--leave inner()...");
    }

    public synchronized void recursive(int n) {
        if (n == 0) return;
        System.out.println(thread() + "@ @ enter recursive( " + n + " )...");
        recursive(n - 1);
    }

    private static String thread() {
        return "[" + Thread.currentThread().getName() + "]";
    }

    public static void main(String[] args) {

        ReentrantBySynchronized r = new ReentrantBySynchronized();
        new Thread(r::outer, "T1").start();
//        outer() 已经持有 this 的监视器锁，调用 inner()/recursive() 时同一线程重复进入，依旧 OK。
//如果这是不可重入锁，这里会自锁死。
//[T1]enter outer()...
//[T1]enter inner()...
//[T1]@ @ enter recursive( 3 )...
//[T1]@ @ enter recursive( 2 )...
//[T1]@ @ enter recursive( 1 )...
//[T1]--leave inner()...
//[T1]====leave outer()...
    }
}
