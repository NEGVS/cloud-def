# 1-使用GRPC
Spring Cloud 通常使用 **Feign**（基于 RESTful HTTP/1.1）或 Spring WebClient 进行微服务通信，但这些方式在高并发或大数据量场景下性能受限。gRPC 的优势使其成为 Spring Cloud 的有力补充：
- 高性能：HTTP/2 多路复用和 Protocol Buffers 二进制序列化显著降低延迟和带宽占用。
- 流式通信：支持单向流、双向流，适合实时数据或批量处理（如订单统计）。
- 类型安全：通过 .proto 文件定义服务契约，生成强类型代码，减少运行时错误。
- Spring Cloud 集成：通过 Spring Boot 和 Spring Cloud 的扩展，可以无缝集成服务发现（Eureka、Consul）、负载均衡和熔断。

场景设计
- 目标：客户端通过 gRPC 调用后端服务，获取订单表按月统计的订单量（如 2023-01: 100）。
- 架构：
    - 服务端：Spring Boot + gRPC，提供订单统计服务，连接 MySQL 数据库。
    - 客户端：Spring Boot + gRPC，通过 Eureka 发现服务并调用。
    - 数据库:
    - 

# 2 项目依赖
  <!-- gRPC -->
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-spring-boot-starter</artifactId>
        <version>2.14.0.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-protobuf</artifactId>
        <version>1.51.0</version>
    </dependency>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-stub</artifactId>
        <version>1.51.0</version>
    </dependency>

    <build>
        <plugins>
            <!-- Protobuf 编译插件 -->
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
                    <protocExecutable>protoc</protocExecutable>
                    <protoSourceRoot>${project.basedir}/src/main/proto</protoSourceRoot>
                    <outputDirectory>${project.build.directory}/generated-sources</outputDirectory>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

- 说明：
    - grpc-spring-boot-starter：简化 gRPC 配置，提供 Spring Boot 集成。
    - protobuf-maven-plugin：编译 .proto 文件生成 Java 代码。

# 3 定义 gRPC 服务（.proto 文件）
在 src/main/proto/order_stats.proto 中定义服务接口：
```proto

syntax = "proto3";

option java_multiple_files = true;
option java_package = "com.example.orderstats";
option java_outer_classname = "OrderStatsProto";

service OrderStatsService {
  // 按月统计订单量（单次调用）
  rpc GetMonthlyOrderStats (StatsRequest) returns (StatsResponse);

  // 流式返回多月统计
  rpc StreamMonthlyOrderStats (StatsRequest) returns (stream StatsResponse);
}

message StatsRequest {
  string start_year_month = 1; // e.g., "2023-01"
  string end_year_month = 2;  // e.g., "2023-12"
}

message StatsResponse {
  string year_month = 1; // e.g., "2023-01"
  int64 order_count = 2;
}

```

---
=====
protobuf 描述了通信协议，包括服务定义、数据类型和 RPC 方法。
- 说明：
  - GetMonthlyOrderStats：返回指定时间段的统计结果。
  - StreamMonthlyOrderStats：流式返回逐月结果，适合大数据量。
  - 使用 protoc 编译生成 Java 代码（如 OrderStatsServiceGrpc）。

# 4- 服务端实现
服务端（Spring Boot + gRPC）实现订单统计逻辑，连接 MySQL 数据库。

配置文件

```yaml
spring:
  application:
    name: order-service
  datasource:
    url: jdbc:mysql://localhost:3306/orders_db
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

grpc:
  server:
    port: 9090
```

server implements 
