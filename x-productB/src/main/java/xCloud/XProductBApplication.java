package xCloud;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;
import xCloud.tools.springX.MyBean;

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

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Autowired
    private Environment env;

    @Autowired
    private MyBean myBean;

    @PostConstruct
    public void init() {
        System.out.println("当前端口为：" + port);
        log.info("x-product-B 启动完成...");
        log.info("\n--api--");
        log.info("\nAPI文档地址: http://localhost:{}{}/doc.html", port, contextPath);
        log.info("\n--nacos URL");
        log.info("\nhttp://localhost:8848/nacos");
        System.out.println("---------------获取并使用Bean");
        myBean.doBusiness();
    }

    public static void main(String[] args) {
        System.out.println("x-product-B start...");
        SpringApplication.run(XProductBApplication.class, args);
    }


}
