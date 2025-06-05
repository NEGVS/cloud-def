package xCloud.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/4 16:42
 * @ClassName AddOneAndDouble
 * 最好的方法之一就是 重构。在上面的 AddComponent 示例中，与其直接在类中添加 addOneAndDouble()，
 * 不如创建一个包含该方法的新类。新类可以注入 AddComponent，或者更准确地说，注入 AddComponent 的代理：
 */
@Component
public class AddOneAndDouble {
    @Resource
    AddComponent addComponent;

    public int addOneAndDouble(int n) {
        return addComponent.addOne(n) + addComponent.addOne(n);
    }
}
