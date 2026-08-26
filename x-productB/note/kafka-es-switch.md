# Kafka & Elasticsearch & Milvus 启动开关配置

## 问题描述
Kafka、Elasticsearch和Milvus比较吃性能，开发时如果不需要这些服务，可以通过配置关闭，让项目正常启动。

## 解决方案

### 方案一：使用dev配置文件（推荐）

#### 1. 启动时指定profile
```bash
# IDEA启动配置
-Dspring.profiles.active=dev

# 命令行启动
java -jar x-productB.jar --spring.profiles.active=dev

# Maven启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### 2. IDEA配置方式
1. 打开 `Run/Debug Configurations`
2. 找到 `Spring Boot` 启动配置
3. 在 `Active profiles` 填入：`dev`
4. 点击 `Apply` 保存

#### 3. application-dev.yml 配置说明
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
      - org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration

kafka:
  enabled: false  # 禁用Kafka

elasticsearch:
  enabled: false  # 禁用Elasticsearch

milvus:
  enabled: false  # 禁用Milvus
```

### 方案二：在原配置文件中添加开关

如果不想使用profile，可以直接在 `application.yml` 末尾添加：

```yaml
# ============开关控制============
kafka:
  enabled: false  # 改为false禁用Kafka

elasticsearch:
  enabled: false  # 改为false禁用Elasticsearch
```

## 技术实现原理

### 1. 条件化Bean注册
使用 `@ConditionalOnProperty` 注解，只有当配置项为true时才注册Bean：

```java
@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {
    // Kafka相关Bean配置
}
```

### 2. 自动配置排除
在dev配置中排除Spring Boot的自动配置：

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
      - org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration
```

### 3. 已添加条件注解的类
以下类已添加 `@ConditionalOnProperty` 注解，会根据配置自动启用/禁用：

**Kafka相关：**
- `KafkaConfig` - Kafka配置类
- `KafkaProducerService` - Kafka生产者服务
- `KafkaConsumerService` - Kafka消费者服务

**Elasticsearch相关：**
- `ElasticsearchConfig` - ES配置类
- `ElasticsearchIndexInitializer` - ES索引初始化

**Milvus相关：**
- `MilvusConfig` - Milvus配置类
- `MilvusController` - Milvus控制器
- `Embedding2Service` - 向量嵌入服务

**健康检查：**
- `InfrastructureHealthChecker` - 基础设施健康检查（已支持可选注入）

## 使用场景

### 开发环境（不启动Kafka、ES和Milvus）
```bash
# 使用dev配置
java -jar x-productB.jar --spring.profiles.active=dev
```

### 测试环境（按需启动）
```bash
# 使用test配置
java -jar x-productB.jar --spring.profiles.active=test
```

### 生产环境（完整功能）
```bash
# 使用prod配置
java -jar x-productB.jar --spring.profiles.active=prod
```

## 注意事项

1. **matchIfMissing = true**：如果没有配置该属性，默认启用（向后兼容）
2. **依赖关系**：如果业务代码中注入了Kafka、ES或Milvus相关的Bean，需要添加 `@Autowired(required = false)` 或使用 `Optional<>` 包装
3. **功能降级**：关闭这些服务后，相关功能会不可用，需要在业务代码中做好降级处理

## 推荐配置

### 本地开发
```yaml
kafka.enabled: false
elasticsearch.enabled: false
milvus.enabled: false
```

### 联调测试
```yaml
kafka.enabled: true
elasticsearch.enabled: false  # 如果ES还没准备好
milvus.enabled: true
```

### 生产环境
```yaml
kafka.enabled: true
elasticsearch.enabled: true
milvus.enabled: true
```

## 验证方法

### 启动日志检查
**Kafka禁用时，不会出现：**
```
✅ Kafka客户端初始化完成
✅ Kafka Topic创建成功
```

**Elasticsearch禁用时，不会出现：**
```
✅ Elasticsearch客户端初始化完成
✅ ES索引创建成功：candidate_resume
```

**Milvus禁用时，不会出现：**
```
✅ Milvus 客户端已优雅关闭
```

### 端口检查
```bash
# 检查Kafka端口（9092）
lsof -i :9092

# 检查Elasticsearch端口（9200）
lsof -i :9200

# 检查Milvus端口（19530）
lsof -i :19530
```

## 故障排查

### 问题1：启动时仍然尝试连接Kafka/ES
**原因**：配置文件没有生效或路径错误

**解决**：
1. 确认 `application-dev.yml` 在 `src/main/resources` 目录下
2. 确认启动参数 `--spring.profiles.active=dev` 正确
3. 查看启动日志确认加载了哪个配置文件

### 问题2：业务代码报NullPointerException
**原因**：注入的Kafka/ES Bean为null

**解决**：
```java
// 方法1：设置required=false
@Autowired(required = false)
private KafkaProducerService kafkaProducerService;

// 方法2：使用Optional包装
@Autowired
private Optional<KafkaProducerService> kafkaProducerService;

// 方法3：使用前判空
if (kafkaProducerService != null) {
    kafkaProducerService.send(...);
}
```

### 问题3：Spring Boot自动配置仍然尝试连接
**原因**：自动配置排除不完整

**解决**：在 `application-dev.yml` 中添加更多排除项：
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
      - org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor
      - org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
      - org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration
```

## 总结

通过配置开关，可以灵活控制Kafka、Elasticsearch和Milvus的启用状态，提高本地开发体验，降低资源消耗。推荐在开发环境使用 `application-dev.yml` 配置文件。
