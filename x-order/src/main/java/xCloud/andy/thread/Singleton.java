package xCloud.andy.thread;



/**
 * @Description 如何实现一个线程安全的单例模式？双重检查锁的实现
 * @Author Andy Fan
 * @Date 2025/3/4 10:30
 * @ClassName Singleton
 */
public class Singleton {

    /**
     * - 单例模式：确保一个类只有一个实例，并提供全局访问点。
     * - 线程安全要求：
     * - 多线程环境下，避免创建多个实例。
     * - 保证访问时的可见性和一致性。
     */

    //使用 volatile 保证可见性和防止指令重排序
    private static volatile Singleton instance;

    //私有构造方法，防止外部实例化
    public Singleton() {
        //防止反射攻击
        if (instance != null) {
            throw new RuntimeException("单例不允许多个实例,单例模式禁止反射创建实例");
        }
    }

    //获取单例实例
    static Singleton getInstance() {
        //第一次检查（无锁，提高性能）
        if (instance == null) {
            //加锁，确保线程安全
            synchronized (Singleton.class) {
                //第二次检查（防止多个线程同时创建）
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

    /**
     * 为什么需要双重检查？
     * - 第一次检查（无锁）：
     * - 如果 instance 已创建，直接返回，避免不必要的同步开销，提高性能。
     * - 第二次检查（加锁后）：
     * - 在同步块内再次检查，确保多个线程进入同步块时不会重复创建实例。
     * <p>
     * volatile 的作用
     * - 可见性：确保 instance 的赋值对所有线程立即可见。
     * - 有序性：防止 instance = new Singleton() 的指令重排序。
     * - 对象创建分为三步：
     * 1. 分配内存。
     * 2. 初始化对象。
     * 3. 将引用赋值给变量。
     * - 无 volatile 时，步骤 2 和 3 可能颠倒，导致其他线程拿到未初始化的对象。
     */

    public static void main(String[] args) {
        Runnable task = () -> {

            Singleton singleton = Singleton.getInstance();
            System.out.println(Thread.currentThread().getName() + " : " + singleton.hashCode());
        };
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task, "thread-" + i);
            threads[i].start();
        }


    }
}
