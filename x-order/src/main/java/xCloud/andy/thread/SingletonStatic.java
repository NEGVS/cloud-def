package xCloud.andy.thread;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/3/4 10:59
 * @ClassName SingletonStatic
 */
public class SingletonStatic {

    private SingletonStatic() {

    }

    private static class SingletonHolder {
        private static final SingletonStatic INSTANCE = new SingletonStatic();
    }

    public static SingletonStatic getInstance() {

        return SingletonHolder.INSTANCE;
    }
}
