package xCloud.tools.springX;

import org.springframework.stereotype.Component;

/**
 * @Description 另一个依赖Bean：DependencyBean.java（用于属性注入）
 * @Author Andy Fan
 * @Date 2025/11/11 17:48
 * @ClassName DependencyBean
 */
@Component
public class DependencyBean {

    public String getValue() {
        return " ccc从依赖Bean注入的值，DependencyBean ccc";
    }
}
