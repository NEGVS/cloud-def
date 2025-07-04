package xCloud;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(
        info = @Info(
                title = "ANDY API",
                version = "1.2.1.x",
                description = "API documentation for my Spring Cloud XPaymentApplication"
        )
)

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}) // 排除数据库自动配置
@EnableDiscoveryClient // 启用服务注册与发现（如 nacos）
@EnableFeignClients    // 启用 Feign 客户端
public class XPaymentApplication {
    public static void main(String[] args) {
        System.out.println("Hello world! XPaymentApplication start...");
        SpringApplication.run(XPaymentApplication.class, args);
        System.out.println("Hello world! XPaymentApplication done...");
    }
}