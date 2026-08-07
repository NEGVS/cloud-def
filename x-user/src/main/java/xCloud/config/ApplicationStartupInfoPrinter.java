package xCloud.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Description 应用启动完成后，打印 Swagger、RabbitMQ、Nacos 等关键访问地址，便于开发调试
 * @Author Andy Fan
 * @Date 2026/08/08
 * @ClassName ApplicationStartupInfoPrinter
 */
@Component
public class ApplicationStartupInfoPrinter implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        Environment env = ctx.getEnvironment();
        // ==========【基础信息】==========
        String appName = env.getProperty("spring.application.name", "x-user");
        // 取【Web容器实际绑定的端口】，而非读配置属性——避免配置缺失时打印出误导性的默认值
        String port = String.valueOf(getServerPort(ctx, env));
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String protocol = "http";
        String localHost = "localhost";
        String ip = localHost;
        try { ip = InetAddress.getLocalHost().getHostAddress(); } catch (UnknownHostException ignored) {}
        // ==========【Swagger / Knife4j】==========
        String swaggerUi = String.format("%s://%s:%s%s/swagger-ui/index.html", protocol, localHost, port, contextPath);
        String knife4j = String.format("%s://%s:%s%s/doc.html", protocol, localHost, port, contextPath);
        String apiDocs = String.format("%s://%s:%s%s/v3/api-docs", protocol, localHost, port, contextPath);
        // ==========【RabbitMQ】==========
        String mqHost = env.getProperty("spring.rabbitmq.host", localHost);
        String mqPort = env.getProperty("spring.rabbitmq.port", "5672");
        String mqVhost = env.getProperty("spring.rabbitmq.virtual-host", "/");
        // RabbitMQ 管理控制台默认端口为 15672（需启用 rabbitmq_management 插件）
        String mqConsole = String.format("%s://%s:15672", protocol, mqHost);
        // ==========【Nacos】==========
        String nacosDiscovery = env.getProperty("spring.cloud.nacos.discovery.server-addr", "127.0.0.1:8848");
        String nacosConfig = env.getProperty("spring.cloud.nacos.config.server-addr", nacosDiscovery);
        String nacosConsole = String.format("%s://%s/nacos", protocol, nacosDiscovery);

        String line = "----------------------------------------------------------------------------------------------------";
        System.out.printf("%n%s%n" +
                        "  应用 [%s] 启动成功！Application is running!%n" +
                        "%s%n" +
                        "  Swagger UI    : %s%n" +
                        "  Knife4j 文档  : %s%n" +
                        "  OpenAPI Docs  : %s%n" +
                        "%s%n" +
                        "  RabbitMQ 连接 : %s:%s   (virtual-host: %s)%n" +
                        "  RabbitMQ 控制台: %s%n" +
                        "%s%n" +
                        "  Nacos 注册中心: %s%n" +
                        "  Nacos 配置中心: %s%n" +
                        "  Nacos 控制台  : %s%n" +
                        "%s%n" +
                        "  本机 IP       : %s%n" +
                        "%s%n%n",
                line,
                appName,
                line,
                swaggerUi,
                knife4j,
                apiDocs,
                line,
                mqHost, mqPort, mqVhost,
                mqConsole,
                line,
                nacosDiscovery,
                nacosConfig,
                nacosConsole,
                line,
                ip,
                line);
    }

    /**
     * 获取Web容器实际监听的端口：优先取运行时真实绑定端口（如 random port），再退回配置属性，最后回退Spring Boot默认8080
     */
    private int getServerPort(ApplicationContext ctx, Environment env) {
        if (ctx instanceof WebServerApplicationContext) {
            try { return ((WebServerApplicationContext) ctx).getWebServer().getPort(); } catch (Exception ignored) {}
        }
        String p = env.getProperty("server.port");
        return p != null ? Integer.parseInt(p) : 8080;
    }
}
