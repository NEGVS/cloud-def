package xCloud.tools.classTest;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/12 09:45
 * @ClassName ClassLoad
 */
public class ClassLoad {
    public static void main(String[] args) {
        System.out.println("1.触发加载：访问静态字段");
        System.out.println(ParentClass.staticVar);// 触发父类加载-验证-验证-准备-解析-初始化
        System.out.println("2.触发子类初始化");
        new ChildClass();// 触发子类全过程（父类已加载）
    }


}
