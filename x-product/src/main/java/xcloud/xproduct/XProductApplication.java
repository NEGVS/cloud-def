package xcloud.xproduct;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@OpenAPIDefinition(
        info = @Info(
                title = "ANDY API",
                version = "1.2.1.x",
                description = "API documentation for my Spring Cloud application"
        )
)
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("xcloud.xproduct.mapper")
public class XProductApplication {

    public static void main(String[] args) {
        System.out.println("x-product start...");
        SpringApplication.run(XProductApplication.class, args);
        System.out.println("x-product done...");

    }

}
