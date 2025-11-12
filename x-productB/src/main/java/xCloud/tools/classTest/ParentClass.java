package xCloud.tools.classTest;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/12 09:51
 * @ClassName ParentClass
 */
public class ParentClass {
    static int staticVar = 10;//准备阶段设为0，初始化阶段赋值为10

    static {
        System.out.println("ParentClass:静态块执行 （初始化阶段）");
    }
}
