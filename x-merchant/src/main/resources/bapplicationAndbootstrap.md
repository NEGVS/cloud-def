是的，`application.yml` 和 `bootstrap.yml` 可以同时在 Spring Boot 应用中使用，它们有不同的用途和加载顺序，互不冲突。结合你的项目上下文（Spring Boot 3.2.9、Spring Cloud、Nacos、MySQL、Docker），以下是详细说明、加载机制以及如何正确使用两者。

---

### 1. `application.yml` 和 `bootstrap.yml` 的区别

- **`application.yml`**：
    - **用途**：用于配置 Spring Boot 应用的常规属性，如端口、数据源、日志级别、SpringDoc 等。
    - **加载时机**：在 Spring Boot 应用上下文初始化时加载，晚于 `bootstrap.yml`。
    - **典型配置**：
        - `server.port`：应用端口。
        - `spring.datasource`：数据库连接。
        - `springdoc`：Swagger UI 设置。
        - `logging.level`：日志级别。

- **`bootstrap.yml`**：
    - **用途**：用于 Spring Cloud 应用的早期配置，主要与外部配置中心（如 Nacos、Consul、Zookeeper）相关，定义如何连接到配置中心或服务发现。
    - **加载时机**：在 Spring Boot 应用启动的**早期阶段**（Bootstrap Context）加载，先于 `application.yml`。
    - **典型配置**：
        - `spring.cloud.nacos.config`：Nacos 配置中心地址。
        - `spring.cloud.nacos.discovery`：Nacos 服务注册与发现。
        - `spring.application.name`：应用名称（常用于配置中心标识）。

- **总结**：
    - `bootstrap.yml` 负责**连接外部配置中心**，加载基础配置。
    - `application.yml` 负责**应用运行时的具体配置**。
    - 两者可以同时使用，`bootstrap.yml` 的配置优先加载，用于初始化配置环境。

---

### 2. 加载顺序

Spring Boot 和 Spring Cloud 的配置加载遵循以下顺序：

1. **Bootstrap Context**：
    - 加载 `bootstrap.yml` 或 `bootstrap.properties`。
    - 初始化 Spring Cloud 组件（如 Nacos 客户端），连接配置中心。
    - 从配置中心（如 Nacos）拉取远程配置（如 `x-merchant.properties`）。

2. **Application Context**：
    - 加载 `application.yml` 或 `application.properties`。
    - 应用本地配置，覆盖或补充远程配置（取决于优先级）。
    - 加载环境特定的配置文件（如 `application-prod.yml`）。

3. **优先级**：
    - 远程配置（如 Nacos）通常优先于本地 `application.yml`。
    - 本地 `application.yml` 可覆盖 `bootstrap.yml` 中的部分属性（但不建议重复配置）。
    - 具体优先级可参考 Spring Boot 文档（）。

---

### 3. 是否可以同时使用？

**答案**：可以，且在 Spring Cloud 项目中（如你的 `x-merchant` 应用使用 Nacos）非常常见。以下是典型场景：

- **`bootstrap.yml`**：定义 Nacos 服务器地址和应用名称，确保应用能连接到 Nacos 拉取配置。
- **`application.yml`**：定义本地数据源、端口、SpringDoc 等配置，作为默认或备用配置。

**注意**：
- 如果配置中心（如 Nacos）不可用，`application.yml` 可作为回退配置。
- 避免在两个文件中配置相同的属性（如 `spring.datasource.url`），以防优先级混淆。

---

### 4. 示例配置

结合你的项目（`x-merchant-0.0.1-SNAPSHOT.jar`，Docker 部署，端口 8087，MySQL，Nacos，SpringDoc），以下是 `application.yml` 和 `bootstrap.yml` 的示例：

#### `bootstrap.yml`

```yaml
spring:
  application:
    name: x-merchant
  cloud:
    nacos:
      config:
        server-addr: nacos:8848
        namespace: public
        group: DEFAULT_GROUP
        file-extension: properties
      discovery:
        server-addr: nacos:8848
```

**说明**：
- `spring.application.name`：应用名，用于 Nacos 配置（如 `x-merchant.properties`）。
- `spring.cloud.nacos.config.server-addr`：连接 Docker 内的 Nacos 服务。
- `spring.cloud.nacos.discovery`：启用服务注册与发现。

#### `application.yml`

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://mysql:3306/x_merchant_db?useSSL=false&serverTimezone=UTC
    username: root
    password: root123
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
server:
  port: 8087
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
logging:
  level:
    com.example: DEBUG
    org.springdoc: DEBUG
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

**说明**：
- `server.port`：运行端口 8087（可调整为 8081 或其他）。
- `spring.datasource`：连接 Docker 内的 MySQL 服务。
- `springdoc`：启用 Swagger UI，解决 `/v3/api-docs/swagger-config` 404。
- `server.shutdown=graceful`：支持平滑重启。
- `management.endpoints`：暴露 Actuator 健康端点。

#### Nacos 配置（可选）

在 Nacos 控制台（`http://localhost:8848/nacos`）添加 `x-merchant.properties`（Data ID：`x-merchant.properties`，Group：`DEFAULT_GROUP`）：

```properties
logging.level.com.example=DEBUG
springdoc.api-docs.enabled=true
```

**说明**：
- Nacos 配置会覆盖 `application.yml` 中的同名属性。
- 本地 `application.yml` 作为默认配置，Nacos 不可用时回退使用。

---

### 5. 注意事项

1. **配置优先级**：
    - Nacos 远程配置 > `application.yml` > `bootstrap.yml`。
    - 避免在 `bootstrap.yml` 和 `application.yml` 中重复配置（如 `spring.datasource.url`），以免调试困难。
    - 可通过日志（`logging.level.org.springframework.boot=DEBUG`）查看加载顺序。

2. **Nacos 依赖**：
    - 确保 `pom.xml` 包含 Nacos 配置依赖：

      ```xml
      <dependency>
          <groupId>com.alibaba.cloud</groupId>
          <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
          <version>2023.0.1.2</version>
      </dependency>
      <dependency>
          <groupId>com.alibaba.cloud</groupId>
          <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
          <version>2023.0.1.2</version>
      </dependency>
      ```

3. **Docker 环境**：
    - 你的应用在 Docker 中运行（`docker-compose.yml` 定义了 `x-merchant`、`mysql`、`nacos`）。
    - 确保 `bootstrap.yml` 中的 `nacos:8848` 和 `application.yml` 中的 `mysql:3306` 使用 Docker 网络服务名。

4. **SpringDoc 问题**：
    - 如果仍遇到 `/v3/api-docs/swagger-config` 404，确保 `springdoc-openapi-starter-webmvc-ui:2.6.0` 依赖无冲突，并在 `application.yml` 中启用 SpringDoc。
    - 检查容器日志（`docker logs <x-merchant-container-id>`）是否有 SpringDoc 初始化错误。

5. **端口调整**：
    - 如果需要切换到端口 `8081`（如验证 `101.35.226.226:8081`），修改 `application.yml` 和 `docker-compose.yml`：
      ```yaml
      server:
        port: 8081
      ```
      ```yaml
      services:
        x-merchant:
          ports:
            - "8081:8081"
      ```

6. **配置文件加载验证**：
    - 启动应用后，检查日志确认 `bootstrap.yml` 和 `application.yml` 加载：
      ```bash
      docker logs <x-merchant-container-id> | grep "Loaded config file"
      ```
    - 验证 Nacos 配置拉取：
      ```bash
      docker logs <x-merchant-container-id> | grep "nacos"
      ```

---

### 6. 验证

1. **启动 Docker 容器**：
    - 使用之前的 `docker-compose.yml`：
      ```bash
      docker-compose up -d
      ```

2. **测试接口**：
    - 访问 `http://localhost:8087/stock/process?dateStr=2025-06-23%2017:17:22`（或 `http://101.35.226.226:8081` 如果端口为 8081）。
    - 确认返回 `Stock` 列表，`logo_type` 更新为 `2025-06-23`。

3. **测试 Swagger UI**：
    - 访问 `http://localhost:8087/swagger-ui.html` 或 `http://101.35.226.226:8081/swagger-ui.html`。
    - 验证 `/v3/api-docs/swagger-config` 是否返回 JSON 配置。

4. **检查 Nacos**：
    - 登录 `http://localhost:8848/nacos`，确认 `x-merchant` 服务注册。
    - 检查 `x-merchant.properties` 配置是否生效。

5. **检查 MySQL**：
    - 连接 `localhost:3306`（或 Docker 内的 `mysql`），查看 `x_merchant_db` 的 `stock` 表。

---

### 如果问题仍未解决

请提供以下信息，我可以进一步排查：

1. 完整的 `pom.xml` 文件。
2. 当前的 `application.yml` 和 `bootstrap.yml` 内容。
3. Docker 容器日志（`docker logs <x-merchant-container-id>`）。
4. Nacos 控制台的配置详情（Data ID、Group）。
5. 是否有配置冲突或加载错误的具体日志。

---

### 总结

`application.yml` 和 `bootstrap.yml` 可以同时使用，`bootstrap.yml` 用于早期加载 Spring Cloud 配置（如 Nacos），`application.yml` 用于应用运行时配置。你的 `x-merchant` 项目已正确配置两者，支持 Docker 部署（端口 8087 或 8081）。确保 Nacos 和 SpringDoc 配置正确，验证接口和 Swagger UI。如有问题，提供更多细节，我会继续协助！