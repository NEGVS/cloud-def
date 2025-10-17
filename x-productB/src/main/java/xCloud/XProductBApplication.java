package xCloud;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@OpenAPIDefinition(
        info = @Info(
                title = "ANDY API XProductBApplication",
                version = "1.1.1.0",
                description = "API documentation for my Spring Cloud XProductBApplication"
        )
)
@EnableFeignClients
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@ServletComponentScan // 启用 Servlet 组件扫描（Druid 的 StatViewServlet 需要）
@MapperScan("xCloud.mapper")
public class XProductBApplication {

    @Value("${server.port}")
    public String port;

    @PostConstruct
    public void init() {
        System.out.println("当前端口为：" + port);
    }

    public static void main(String[] args) {
        System.out.println("x-product-B start...");
        ConfigurableApplicationContext context = SpringApplication.run(XProductBApplication.class, args);
        log.info("x-product-B 启动完成...");
        Environment env = context.getEnvironment();
        String actualPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        log.info("\n--api--");
        log.info("\nAPI文档地址: http://localhost:{}{}/doc.html", actualPort, contextPath);
        log.info("\n--nacos URL");
        log.info("\nhttp://localhost:8848/nacos");
    }


}
