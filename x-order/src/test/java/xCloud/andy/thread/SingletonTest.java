package xCloud.andy.thread;

import static org.junit.jupiter.api.Assertions.*;

class SingletonTest {


    void getInstance() {

        Runnable task =()->{
            Singleton singleton = Singleton.getInstance();
        };
    }



}