package xCloud;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(
        info = @Info(
                title = "ANDY API",
                version = "1.2.1.x",
                description = "API documentation for my Spring Cloud XPaymentApplication"
        )
)
@SpringBootApplication
//启用nacos
//收拾收
//启用 Feign
@EnableFeignClients
public class XPaymentApplication {
    public static void main(String[] args) {
        System.out.println("Hello world! XPaymentApplication start...");
        SpringApplication.run(XPaymentApplication.class, args);
        System.out.println("Hello world! XPaymentApplication done...");
    }
}