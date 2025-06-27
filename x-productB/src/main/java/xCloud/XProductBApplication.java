package xCloud;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@OpenAPIDefinition(
        info = @Info(
                title = "ANDY API X XProductBApplication",
                version = "1.1.1.0",
                description = "API documentation for my Spring Cloud  X XProductBApplication"
        )
)
@SpringBootApplication
@EnableDiscoveryClient
@ServletComponentScan // 启用 Servlet 组件扫描（Druid 的 StatViewServlet 需要）
@MapperScan("xCloud.mapper")
public class XProductBApplication {

//    @Value("${server.port}")
//    private String port;
//
//    @PostConstruct
//    public void init() {
//        System.out.println("当前端口为：" + port);
//    }

    public static void main(String[] args) {
        System.out.println("x-product-B start...");
        SpringApplication.run(XProductBApplication.class, args);
        System.out.println("x-product-B done...");
    }

}
