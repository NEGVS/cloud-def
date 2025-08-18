# 1-GateWay

https://yintianwen.top/post/5c5c0c0.html

# 2-阿里限流神器Sentinel
https://juejin.cn/post/7018406221120045087

# 深入理解 Sentinel 中的限流算法
https://yintianwen.top/post/690c857.html

Sentinel 系统自适应过载保护从整体维度对应用入口流量进行控制，结合应用的 Load、CPU 使用率、总体平均 RT、入口 QPS 和
并发线程数等几个维度的监控指标，通过自适应的流控策略，
让系统的入口流量和系统的负载达到一个平衡，让系统尽可能跑在最大吞吐量的同时保证系统整体的稳定性。



好的，我们来一步步创建一个 **Spring Cloud Gateway** 服务。这里假设你使用的是 **Spring Boot 3 + Spring Cloud 2022.x** 系列，并使用 **Maven** 构建项目。下面我给你一个完整的示例和说明。

---

## 1️⃣ 创建 Spring Boot 项目

* **项目名称**: `gateway-service`
* **依赖**：

    * Spring Boot Starter Web
    * Spring Boot Starter Actuator
    * Spring Cloud Gateway
    * Spring Boot Config Client（如果有配置中心）
    * Spring Boot Starter Test

你可以在 `pom.xml` 中这样配置：

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>gateway-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2022.0.4</spring-cloud.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

> 注意：Spring Cloud Gateway 基于 **WebFlux**，所以使用 `spring-boot-starter-webflux` 而不是普通 `spring-boot-starter-web`。

---

## 2️⃣ 创建启动类

```java
package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
```

---

## 3️⃣ 配置路由

在 `application.yml` 中配置：

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway-service

  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/user/**      # 匹配 /user/** 的请求
          filters:
            - StripPrefix=1      # 去掉 /user 前缀

        - id: order-service
          uri: http://localhost:8082
          predicates:
            - Path=/order/**     # 匹配 /order/** 的请求
          filters:
            - StripPrefix=1
```

解释：

* **routes**：定义路由规则
* **id**：路由 ID
* **uri**：要转发到的微服务地址
* **predicates**：匹配规则
* **filters**：对请求的过滤，例如去掉前缀

---

## 4️⃣ 添加网关全局过滤器（可选）

可以实现日志记录、鉴权、限流等：

```java
package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Request Path: {}", exchange.getRequest().getPath());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // 优先级
    }
}
```

---

## 5️⃣ 测试网关

假设有两个服务：

* `user-service` 启动在 `http://localhost:8081`
* `order-service` 启动在 `http://localhost:8082`

请求：

```
GET http://localhost:8080/user/info  -> 会被转发到 http://localhost:8081/info
GET http://localhost:8080/order/list -> 会被转发到 http://localhost:8082/list
```

---

## 6️⃣ 可选：启用 Actuator 和监控

在 `application.yml` 添加：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

访问：`http://localhost:8080/actuator/routes` 可以查看所有路由信息。

---

如果你愿意，我可以帮你生成一个 **完整可运行的 Spring Cloud Gateway 示例项目**，包含 **多个微服务 + Eureka 注册中心 + Gateway 动态路由**，这样你直接拉下来就能跑起来。

你希望我帮你生成吗？
