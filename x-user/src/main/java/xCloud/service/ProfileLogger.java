package xCloud.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/4/7 11:25
 * @ClassName ProfileLogger
 */
@Component
public class ProfileLogger {
    @Autowired
    private Environment env;

    @PostConstruct
    public void logProfiles() {
        System.out.println("激活的 Profile: " +
                String.join(", ", env.getActiveProfiles()));
    }
}
