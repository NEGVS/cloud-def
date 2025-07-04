# gRPC Microservice Demo

## 服务说明

- `user-service`: 提供用户信息
- `order-service`: 作为客户端调用 `user-service` 的 gRPC 接口

## 启动顺序

1. 启动 `user-service`
2. 启动 `order-service`
3. `order-service` 启动后会调用 `user-service`，打印返回的用户信息

## 后续可集成

- JWT 认证拦截器
- 链路追踪（Spring Cloud Sleuth / OpenTelemetry）
- 限流熔断（Sentinel）
