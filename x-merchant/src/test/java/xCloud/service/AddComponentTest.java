package xCloud.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import xCloud.service.selfInjection.AddComponent;
import xCloud.service.selfInjection.AddOneAndDouble;
import xCloud.service.selfInjection.SelfInjection;

@SpringBootTest
class AddComponentTest {
    @Resource
    AddComponent addComponent;
    @Resource
    AddOneAndDouble addOneAndDouble;
    @Resource
    SelfInjection selfInjection;

    @Test
    void dkk() {
        addComponent.resetCache();
        int i = addComponent.addOne(0);
        int i2 = addComponent.addOne(0);
        System.out.println(addComponent.getCounter());
    }

    @Test
    void whenInternalCall_thenCacheNotHit() {
        addComponent.resetCache();
        addComponent.addOneAndDouble(0);
        int counter = addComponent.getCounter();
        System.out.println(counter);
        addComponent.resetCache();

        int i = addOneAndDouble.addOneAndDouble(0);
        System.out.println(i);
        selfInjection.resetCache();
        int i1 = selfInjection.addOneAndDouble(0);
        System.out.println(i1);
    }
}