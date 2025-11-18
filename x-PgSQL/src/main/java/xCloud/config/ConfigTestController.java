package xCloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/14 14:27
 * @ClassName ConfigTestController
 */
@RestController
public class ConfigTestController {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @GetMapping("/check-db-url")
    public String checkDbUrl() {
        return dbUrl;
    }
}
