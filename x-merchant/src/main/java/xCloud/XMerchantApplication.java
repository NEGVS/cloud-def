package xCloud;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@OpenAPIDefinition(
        info = @Info(
                title = "ANDY API X Merchants",
                version = "1.1.1.x",
                description = "API documentation for my Spring Cloud  X Merchants Application"
        )
)
//启用nacos
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("xCloud.mapper")
public class XMerchantApplication {
    public static void main(String[] args) {
        System.out.println("Hello world! XMerchantApplication start...");
        SpringApplication.run(XMerchantApplication.class, args);
        System.out.println("Hello world! XMerchantApplication done...");
    }
}