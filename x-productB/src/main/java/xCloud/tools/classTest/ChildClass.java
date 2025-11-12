package xCloud.tools.classTest;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/12 09:51
 * @ClassName ChildClass
 */
public class ChildClass  extends ParentClass {
    static {
        System.out.println("ChildClass:静态块执行 （初始化阶段）");
    }

    public ChildClass() {
        System.out.println("ChildClass:实例化");
    }
}
