package xCloud.tools.springX;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @Description 自定义BeanPostProcessor：MyBeanPostProcessor.java
 * 用于在初始化前后干预（例如，打印日志或增强）。
 * @Author Andy Fan
 * @Date 2025/11/11 17:51
 * @ClassName MyBeanPostProcessor
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof MyBean) {
            System.out.println("6. BeanPostProcessor: postProcessBeforeInitialization 执行，Bean=" + beanName);
        }
        return bean;// 返回原Bean或修改后
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof MyBean) {
            System.out.println("9. BeanPostProcessor: postProcessAfterInitialization 执行，Bean=" + beanName);
        }
        return bean;
    }
}
