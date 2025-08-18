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

# 完整可运行的 Spring Cloud Gateway 示例项目，包含 多个微服务 + nacos注册中心 + Gateway 动态路由

明白了，你想要一个完整示例：**Spring Cloud Gateway + 多个微服务 + Nacos 注册中心 + 动态路由**。我给你设计一个完整的项目结构和配置示例，你可以直接运行。

---

# 项目结构

```
springcloud-demo/
├── nacos/                     # Nacos 单机启动配置（可选）
├── gateway-service/           # 网关服务
├── user-service/              # 用户微服务
├── order-service/             # 订单微服务
├── pom.xml                    # 父级 POM
```

---

# 1️⃣ 父级 `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>springcloud-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>gateway-service</module>
        <module>user-service</module>
        <module>order-service</module>
    </modules>

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
</project>
```

---

# 2️⃣ 各服务 `pom.xml`（以 gateway-service 为例）

```xml
<project ...>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>springcloud-demo</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>gateway-service</artifactId>

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
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
    </dependencies>
</project>
```

> user-service 和 order-service 只需要 `spring-boot-starter-web` 和 `spring-cloud-starter-alibaba-nacos-discovery`。

---

# 3️⃣ Nacos 注册中心配置

假设本地 Nacos 启动在：`http://localhost:8848`

`application.yml` 配置示例：

```yaml
spring:
  application:
    name: gateway-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

user-service、order-service 同样配置 `spring.application.name` 和 `spring.cloud.nacos.discovery.server-addr`。

---

# 4️⃣ Gateway 动态路由配置

在 `gateway-service` 的 `application.yml`:

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true   # 启用动态路由，自动根据注册中心服务生成路由
          lower-case-service-id: true
```

这样，Gateway 会自动把 Nacos 注册的服务（user-service、order-service）生成路由：

```
http://localhost:8080/user-service/xxx -> 转发到 user-service
http://localhost:8080/order-service/xxx -> 转发到 order-service
```

---

# 5️⃣ 微服务示例

### user-service

```java
@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of("id", "1", "name", "Alice");
    }
}
```

### order-service

```java
@RestController
@RequestMapping("/order")
public class OrderController {
    @GetMapping("/list")
    public List<String> getOrders() {
        return List.of("Order001", "Order002");
    }
}
```

---

# 6️⃣ 启动顺序

1. 启动 Nacos
2. 启动 user-service
3. 启动 order-service
4. 启动 gateway-service

测试访问：

```
GET http://localhost:8080/user-service/user/info
GET http://localhost:8080/order-service/order/list
```

> Gateway 会自动路由到对应服务。

---

# ✅ 可选优化

1. 添加 **全局过滤器** 做日志、限流或鉴权。
2. 配置 **网关熔断和重试**。
3. 使用 **Spring Cloud Alibaba Nacos 配置管理**，实现路由动态刷新。

---

# 我可以帮你生成一个 **完整可运行的 Maven 项目源码**，里面包含 `gateway-service`、`user-service`、`order-service`，和 Nacos 配置，直接下载就能运行。

你希望我直接生成吗？

明白了，我们在你的 **Spring Cloud Gateway + Nacos** 项目上进一步增强：

1. **全局过滤器**（日志、限流、鉴权）
2. **熔断和重试**
3. **Nacos 配置管理，实现路由动态刷新**

下面我给你完整示例。

---

## 1️⃣ 添加依赖（gateway-service 的 pom.xml）

```xml
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
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

---

## 2️⃣ 全局过滤器示例

在 `gateway-service` 下创建 `GlobalFilterConfig.java`：

```java
package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class LoggingRateLimitAuthFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRateLimitAuthFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        logger.info("Request path: {}", path);

        // 简单鉴权示例
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || !token.equals("Bearer admin-token")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 简单限流示例：每次延迟50ms（真实项目可使用Redis/令牌桶）
        return Mono.delay(Duration.ofMillis(50)).then(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
```

> ✅ 功能：
>
> * 记录日志
> * 简单鉴权
> * 简单限流（模拟延迟）

---

## 3️⃣ 配置熔断和重试

在 `application.yml` 中配置动态路由：

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      default-filters:
        - name: Retry
          args:
            retries: 2
            statuses: BAD_GATEWAY, INTERNAL_SERVER_ERROR
            series: SERVER_ERROR
        - name: CircuitBreaker
          args:
            name: defaultCircuit
            fallbackUri: forward:/fallback
```

在 `GatewayServiceApplication` 中添加回退接口：

```java
@RestController
public class FallbackController {
    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("服务暂时不可用，请稍后再试");
    }
}
```

---

## 4️⃣ 使用 Nacos 配置管理实现路由动态刷新

1. **Nacos 配置示例**（Data ID: `gateway-dynamic-routes.yml`）

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/user/**

        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/order/**
```

> `lb://` 表示使用 **Nacos 服务发现的负载均衡**

2. **gateway-service application.yml**

```yaml
spring:
  application:
    name: gateway-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yaml
        shared-configs:
          - data-id: gateway-dynamic-routes.yml
            refresh: true   # 开启动态刷新
```

> 当你在 Nacos 上修改 `gateway-dynamic-routes.yml` 配置，Gateway 会自动刷新路由，无需重启。

---

## 5️⃣ 完整请求示例

* `GET http://localhost:8080/user/info`
* 需在 Header 中添加：`Authorization: Bearer admin-token`
* 当服务挂掉，触发熔断 `/fallback`
* 支持重试 2 次

---

我可以帮你把这个 **完整项目源码**（含 gateway、user、order 服务 + Nacos 配置 + 全局过滤器 + 熔断重试 + 动态刷新）直接生成成可运行的 Maven 项目，拉下来就能跑。

# 你希望我直接生成吗？
已为你生成一个**可直接运行的 Maven 多模块项目**（Spring Cloud Gateway + Nacos + 全局过滤器 + 熔断重试 + 动态路由刷新）。

**下载：** [springcloud-nacos-gateway-demo.zip](sandbox:/mnt/data/springcloud-nacos-gateway-demo.zip)

### 内含内容

* `gateway-service`（WebFlux + Gateway + Nacos Discovery/Config + Resilience4j）

  * 全局过滤器：日志 + 简单鉴权（`Authorization: Bearer admin-token`）+ 简单限流（每 IP 每分钟 120 次）
  * 默认全局过滤器：`Retry`（2 次）+ `CircuitBreaker`（fallback → `/fallback`）
  * 开启 `discovery.locator` + 从 **Nacos** 导入动态路由（`spring.config.import: nacos:gateway-dynamic-routes.yml`）
* `user-service`、`order-service`：示例接口、注册到 Nacos
* `README.md`：启动与 Nacos 配置步骤

### 快速运行

1. 启动 Nacos（默认 `127.0.0.1:8848`）。
2. 在 Nacos 配置中心新增 `gateway-dynamic-routes.yml`（示例已写在 README）。
3. 在项目根目录执行：

```bash
mvn -q -DskipTests package
java -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar
java -jar gateway-service/target/gateway-service-0.0.1-SNAPSHOT.jar
```

### 测试（需请求头）

在请求头里添加：`Authorization: Bearer admin-token`

* 动态路由（Nacos 配置生效时）

  * `GET http://localhost:8080/user/info`
  * `GET http://localhost:8080/order/list`
* 服务发现自动路由（locator）

  * `GET http://localhost:8080/user-service/user/info`
  * `GET http://localhost:8080/order-service/order/list`

需要把鉴权、限流或熔断策略改成你们生产规范？告诉我规则（白名单、密钥校验、限流阈值、降级页等），我直接给你改好。





