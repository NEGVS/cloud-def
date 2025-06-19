package xCloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@OpenAPIDefinition(
        info = @Info(
                title = "ANDY API X User",
                version = "1.2.1.x",
                description = "API documentation for my Spring Cloud  X User Application"
        )
)
//启用nacos
//@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("xCloud.mapper")
public class UserApplication {
    public static void main(String[] args) {
        System.out.println("Hello world! UserApplication start...");
        SpringApplication.run(UserApplication.class, args);
        System.out.println("Hello world! UserApplication done...");
    }
}