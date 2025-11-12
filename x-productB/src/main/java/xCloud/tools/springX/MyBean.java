package xCloud.tools.springX;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/11 17:25
 * @ClassName MyBean
 */
@Component
public class MyBean implements BeanNameAware, BeanFactoryAware, ApplicationContextAware, InitializingBean, DisposableBean {

    private String beanName;

    private BeanFactory beanFactory;

    private ApplicationContext applicationContext;

    // 属性注入示例（假设有另一个Bean作为依赖）
    // 属性注入：使用 @Value 注解直接注入（修正点）
    @Value("从依赖Bean注入的值")
    private String injectedProperty = "kkk默认值";// 默认值作为 fallback

    @Autowired
    private DependencyBean dependency;  // 注入 Bean

    public MyBean() {
        System.out.println("1. 实例化：MyBean 构造函数被调用");
    }

    // BeanNameAware 接口方法
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("2. BeanNameAware: Bean名称设置为 " + name);

    }

    // BeanFactoryAware 接口方法
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        System.out.println("3. BeanFactoryAware: BeanFactory 已注入");
    }

    // ApplicationContextAware 接口方法
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        System.out.println("4. ApplicationContextAware: ApplicationContext 已注入");
    }

    // 属性注入示例,spring  启动时自动调用setter或字段注入
    public void setInjectedProperty(String injectedProperty) {
        this.injectedProperty = injectedProperty;
        System.out.println("5. 属性注入：injectedProperty 注入的属性值为 " + injectedProperty);
    }

    // InitializingBean 接口方法(初始化后调用)
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("7. InitializingBean: afterPropertiesSet() 方法被调用");
    }


//    @PostConstruct  // 或在 afterPropertiesSet 中
//    public void initFromDependency() {
//        this.injectedProperty = dependency.getValue();
//        System.out.println("5. 属性注入: injectedProperty 设置为 " + injectedProperty);  // 移到这里
//    }

    // @PostConstruct 注解方法（自定义初始化，InitializingBean 后调用）
    @PostConstruct
    public void postConstructInit() {
        System.out.println("8. @PostConstruct: postConstructInit() 自定义初始化方法执行");
    }

    // DisposableBean 接口方法（销毁时调用）
    @Override
    public void destroy() throws Exception {
        System.out.println("9.销毁阶段 DisposableBean: destroy() 方法被调用");
    }

    //@PreDestroy 注解方法（自定义销毁，DisposableBean 后调用）
    @PreDestroy
    public void preDestroy() {
        System.out.println("10.销毁阶段 @PreDestroy: preDestroy() 自定义销毁方法执行");
    }

    // 业务方法（Bean 使用阶段）
    public void doBusiness() {
        System.out.println("12.Bean 使用阶段: 执行业务逻辑，beanName=" + beanName + ", injectedProperty=" + injectedProperty);
    }
}
