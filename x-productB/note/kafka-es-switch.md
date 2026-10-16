# Kafka & Elasticsearch & Milvus 启动总开关

## 问题描述

Kafka、Elasticsearch、Milvus 都是外部重型中间件，本地开发时若不需要，启动阶段会出现连接重试、超时告警，甚至 Bean 注入失败导致项目起不来。需要通过一套统一的开关让项目在缺少这些服务时也能正常启动、正常运行。

## 开关清单

| 配置项 | 作用 | 关闭后行为 |
|--------|------|-----------|
| `kafka.enabled` | 控制 Kafka 全链路 | 不创建 `KafkaTemplate` / `AdminClient` / 监听容器工厂 / Topic；消费者不注册；生产者静默跳过发送 |
| `elasticsearch.enabled` | 控制 Elasticsearch | 不创建 `ElasticsearchClient`；`CandidateResumeRepository` 等 Repository 不注册；依赖 ES 的服务不注册 |
| `milvus.enabled` | 控制 Milvus | 不创建 `MilvusClientV2`；依赖 Milvus 的服务不注册 |

**默认值策略：`matchIfMissing = true`** —— 配置项**不写**时视为 `true`（启用）。
这保证了生产环境、以及任何未显式配置的环境，行为与改造前**完全一致**，不会有任何回归风险。

## 使用方式

### 方式一：dev 配置文件（推荐，本地开发）

`src/main/resources/application-dev.yml` 中已配置三个开关为 `false`：

```yaml
kafka:
  enabled: false
elasticsearch:
  enabled: false
milvus:
  enabled: false
```

启动时指定 profile：

```bash
# 命令行
java -jar x-productB.jar --spring.profiles.active=dev

# Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

IDEA 配置方式：`Run/Debug Configurations` → `Spring Boot` → `Active profiles` 填入 `dev`。

### 方式二：直接在主配置里改

在 `application.yml` 末尾（`# ============基础设施总开关============` 处）按需改：

```yaml
kafka:
  enabled: false
elasticsearch:
  enabled: true    # 单独保留 ES
milvus:
  enabled: true
```

### 方式三：启动参数临时覆盖（不改文件）

```bash
java -jar x-productB.jar \
  --kafka.enabled=false \
  --elasticsearch.enabled=false \
  --milvus.enabled=false
```

## 技术实现原理

### 1. 配置类整体条件注册

```java
@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {
    // KafkaTemplate / AdminClient / Topic 全部不注册
}
```

`KafkaConfig` 整体不注册后，`spring-boot-starter-kafka` 的 `KafkaAutoConfiguration` 会因
`@ConditionalOnMissingBean` 条件反转而**接管自动配置**——但它在没有 `spring.kafka.*` 显式配置、
且不主动调用时不建立连接，所以不会产生副作用。为避免歧义，本项目通过 `KafkaProducerService`
的 `ObjectProvider` 判空短路，从业务层彻底切断调用。

### 2. 依赖 Bean 的服务用 ObjectProvider 延迟获取

Bean 可能不存在时，**不能**用 `@Autowired` / `@Resource` 直接注入（会抛 `NoSuchBeanDefinitionException`），
改用 `ObjectProvider<T>`，在**方法内**按需取：

```java
@Autowired
private ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

private void sendMessage(String topic, String key, Object message) {
    KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
    if (kafkaTemplate == null) {
        log.debug("Kafka未启用，跳过消息发送：topic={}, key={}", topic, key);
        return;   // 静默降级，业务主流程不受影响
    }
    // ... 正常发送
}
```

### 3. 双开关依赖用 @ConditionalOnExpression

`HybridSearchService` 同时依赖 ES 和 Milvus，而 `@ConditionalOnProperty` **不可重复标注**
（重复会被后者覆盖），因此用 SpEL 表达"与"关系：

```java
@ConditionalOnExpression("'${elasticsearch.enabled:true}' == 'true' and '${milvus.enabled:true}' == 'true'")
```

### 4. TODO 状态说明（已全部落地）

以下类已完成条件化改造，与本文档描述一致：

**Kafka：**
- `KafkaConfig` — `@ConditionalOnProperty(kafka.enabled)`
- `KafkaConsumerService` — `@ConditionalOnProperty(kafka.enabled)`（`@KafkaListener` 不再注册）
- `KafkaProducerService` — 始终注册，`ObjectProvider<KafkaTemplate>` 判空降级

**Elasticsearch：**
- `ElasticsearchConfig` — `@ConditionalOnProperty(elasticsearch.enabled)`（含 `@EnableElasticsearchRepositories`，Repository 随之不注册）
- `ElasticsearchIndexInitializer` — 同上
- `HybridSearchService` — ES + Milvus 双开关
- `CandidateScoringService` — 依赖 `CandidateResumeRepository`，跟随 ES 开关

**Milvus：**
- `MilvusConfig` — 待接入 `milvus.enabled`（当前用 `@Lazy` 保证不阻塞启动）

**健康检查：**
- `InfrastructureHealthChecker` — 三个客户端全部改用 `ObjectProvider`，开关关闭时输出"未启用（已通过开关关闭）"，不做任何连接尝试

**Controller 降级（始终注册，接口级降级）：**
- `HybridSearchController` — 检索接口返回 `enabled=false` 提示；`/health` 始终可用并返回 `hybridSearchEnabled`
- `CandidateScoringController` — 评分接口返回错误提示；纯 DB 查询接口不受影响
- `RecruitmentTestController` — 依赖基础设施的测试返回明确提示；JD 生成、简历上传等始终可用

## 使用场景对照

| 场景 | kafka | elasticsearch | milvus |
|------|-------|---------------|--------|
| 本地开发（只调 LLM/RAG） | false | false | false |
| 联调（ES 未就绪） | true | false | false |
| 完整功能验证 | true | true | true |
| 生产 | 不配置（默认 true） | 不配置 | 不配置 |

## 验证方法

### 启动日志检查

关闭后**不应**出现：
```
✅ Kafka客户端初始化完成
✅ Kafka Topic创建成功
✅ Elasticsearch客户端初始化完成
✅ ES索引创建成功：candidate_resume
```

**应该**出现（健康检查段落）：
```
Elasticsearch：未启用（已通过开关关闭）
Kafka：未启用（已通过开关关闭）
Milvus：未启用（已通过开关关闭）
```

### 端口检查

```bash
lsof -i :9092    # Kafka
lsof -i :9200    # Elasticsearch
lsof -i :19530   # Milvus
```

## 故障排查

### 问题1：启动了但仍在连接 Kafka/ES

**排查**：
1. 确认 `application-dev.yml` 在 `src/main/resources` 下
2. 确认启动参数 `--spring.profiles.active=dev` 生效（看启动日志里的 profile 行）
3. 确认没有在 Nacos 配置中心里又把这几个开关覆盖成 `true`（**Nacos 配置优先级高于本地文件**）

### 问题2：NoSuchBeanDefinitionException

**原因**：业务代码里用 `@Autowired` / `@Resource` 直接注入了不存在的 Bean。

**解决**：改用 `ObjectProvider<T>` + 方法内 `getIfAvailable()` 判空，或给注入点加
`@Autowired(required = false)`。

### 问题3：接口返回"未启用"提示

**这是预期行为**，不是故障。降级响应形如：

```json
{
  "enabled": false,
  "total": 0,
  "results": [],
  "message": "混合检索未启用：请开启 elasticsearch.enabled 与 milvus.enabled"
}
```

需要该功能时把对应开关打开并重启即可。

## 总结

三个开关统一命名、统一语义、统一默认值（缺省即启用），
关闭后相关 Bean 完全不注册，业务侧通过 `ObjectProvider` 判空降级，
做到了"关得干净、开得还原、调用方不判空也不报错"。
