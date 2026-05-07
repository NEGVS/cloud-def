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
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import xCloud.tools.springX.MyBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
        SpringApplication app = new SpringApplication(XProductBApplication.class);
        app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
            String portStr = event.getEnvironment().getProperty("server.port", "8083");
            killProcessOnPort(Integer.parseInt(portStr));
        });
        app.run(args);
    }

    private static void killProcessOnPort(int port) {
        try {
            Process findProcess = Runtime.getRuntime().exec(new String[]{"lsof", "-ti", "tcp:" + port});
            BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
            String pid;
            while ((pid = reader.readLine()) != null) {
                pid = pid.trim();
                if (!pid.isEmpty()) {
                    Runtime.getRuntime().exec(new String[]{"kill", "-9", pid});
                    System.out.println("已 kill 占用端口 " + port + " 的进程 PID: " + pid);
                }
            }
        } catch (Exception e) {
            System.out.println("端口检测异常: " + e.getMessage());
        }
    }


}
