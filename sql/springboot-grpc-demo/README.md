# Spring Boot gRPC 示例项目

## 功能

- 使用 Spring Boot 构建 gRPC 服务和客户端
- 使用 Protobuf 自动生成 Java 类
- 内部通信通过 gRPC 高性能传输

## 启动方式

```bash
# 编译并生成 proto 类
mvn clean install

# 启动服务
mvn spring-boot:run
```

## Docker 构建

```bash
mvn clean package -DskipTests
docker build -t grpc-demo .
docker run -p 9090:9090 grpc-demo
```

## 使用说明

项目启动后，客户端会自动调用服务端的 getUser 方法并输出结果。
