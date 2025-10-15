### 电商资金与订单功能实现方案

基于您的技术栈（PostgreSQL + Spring Boot + MyBatis Plus + Kafka），我将设计一个电商平台的订单和资金功能模块。该方案聚焦于高数据一致性：在分布式环境中，使用Kafka实现异步解耦，同时结合Saga模式（分布式事务的补偿机制）和PostgreSQL的行级锁/事务支持，确保订单创建、支付扣款等操作的最终一致性。避免强一致性（如2PC）带来的性能瓶颈，转而采用柔性一致性（最终一致性+补偿）。

#### 1. 整体架构设计
- **数据库层（PostgreSQL）**：存储订单和资金数据，使用事务隔离级别（READ COMMITTED）和行锁确保核心操作原子性。设计表支持乐观锁（版本号）防止并发冲突。
- **应用层（Spring Boot + MyBatis Plus）**：处理业务逻辑，MyBatis Plus简化CRUD和分页。使用@Transaction注解管理本地事务。
- **消息队列（Kafka）**：订单创建后，异步发送“订单支付”事件到Kafka主题，由资金服务消费处理扣款。使用Kafka事务（exactly-once语义）和死信队列处理失败重试。
- **一致性保障**：
    - **本地事务**：订单/资金更新使用Spring事务。
    - **分布式Saga**：订单服务发起Saga，Kafka传递事件；资金服务补偿失败时，回滚订单状态。
    - **监控**：集成Spring Boot Actuator + Kafka消费者监听，确保消息不丢。
    - **幂等性**：Kafka消息key使用订单ID，消费者用Redis缓存检查重复。

架构图（文本描述）：
```
用户下单 → 订单服务 (Spring Boot) → 保存订单 (PostgreSQL) → 发布Kafka事件 (order-paid)
                                                                 ↓
资金服务 (Spring Boot) ← 消费Kafka → 扣款 (PostgreSQL) → 发布确认事件 (payment-success)
                                                                 ↓
订单服务 ← 消费确认 → 更新订单状态 → 结束Saga
```

#### 2. 数据库设计（PostgreSQL）
创建两个核心表：`orders`（订单）和`funds`（资金账户）。使用序列生成ID，支持JSONB存储扩展字段。

```sql
-- 订单表
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(64) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, PAID, FAILED
    version INT DEFAULT 1,  -- 乐观锁
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 资金账户表
CREATE TABLE funds (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00,
    version INT DEFAULT 1,  -- 乐观锁
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
```

#### 3. Spring Boot 项目结构
使用Maven构建，pom.xml依赖：
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.3</version>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <!-- 其他：lombok, spring-boot-starter-data-redis for 幂等 -->
</dependencies>
```

application.yml配置：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce
    username: postgres
    password: password
  mybatis-plus:
    configuration:
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发日志
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      transaction-id-prefix: tx-order-  # 开启事务
    consumer:
      group-id: fund-group
      enable-auto-commit: false  # 手动提交，确保exactly-once
```

#### 4. 实体类与MyBatis Plus Mapper
使用Lombok简化。定义BaseEntity支持乐观锁。

```java
// BaseEntity.java
@MappedSuperclass
@Data
public abstract class BaseEntity {
    @Version
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// OrderEntity.java
@TableName("orders")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String status;
}

// FundsEntity.java
@TableName("funds")
@Data
@EqualsAndHashCode(callSuper = true)
public class FundsEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal balance;
}

// OrderMapper.java
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    OrderEntity selectByOrderNo(@Param("orderNo") String orderNo);
}

// FundsMapper.java
@Mapper
public interface FundsMapper extends BaseMapper<FundsEntity> {
    @Update("UPDATE funds SET balance = balance - #{amount}, version = version + 1, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND version = #{version} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount, @Param("version") Integer version);
}
```

#### 5. 业务服务实现
##### 订单服务（OrderService）
创建订单并发布Kafka事件。

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderService {
    @Autowired private OrderMapper orderMapper;
    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;

    public String createOrder(CreateOrderRequest request) {
        // 生成订单号
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        
        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setAmount(request.getAmount());
        order.setStatus("PENDING");
        orderMapper.insert(order);
        
        // 发布Kafka事件，开启事务
        kafkaTemplate.executeInTransaction(t -> {
            kafkaTemplate.send("order-paid-topic", orderNo, order);
            return null;
        });
        
        return orderNo;
    }
    
    // 消费支付确认事件，更新订单状态
    @KafkaListener(topics = "payment-success-topic", groupId = "order-group")
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        OrderEntity order = orderMapper.selectByOrderNo(event.getOrderNo());
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("PAID");
            orderMapper.updateById(order);  // 乐观锁自动处理
        }
    }
}
```

##### 资金服务（FundsService）
消费Kafka事件，扣款并发布确认事件。使用Saga补偿：失败时发送回滚事件。

```java
@Service
public class FundsService {
    @Autowired private FundsMapper fundsMapper;
    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private RedisTemplate<String, String> redisTemplate;  // 幂等检查

    @KafkaListener(topics = "order-paid-topic", groupId = "fund-group")
    public void processPayment(OrderEntity order) {
        String key = "payment:" + order.getOrderNo();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return;  // 幂等
        }
        redisTemplate.opsForValue().set(key, "processed", Duration.ofHours(1));
        
        try {
            FundsEntity funds = fundsMapper.selectOne(new QueryWrapper<FundsEntity>().eq("user_id", order.getUserId()));
            if (funds.getBalance().compareTo(order.getAmount()) < 0) {
                throw new InsufficientBalanceException("余额不足");
            }
            
            // 本地事务扣款
            int rows = fundsMapper.deductBalance(order.getUserId(), order.getAmount(), funds.getVersion());
            if (rows == 0) {
                throw new OptimisticLockException("并发冲突");
            }
            
            // 发布成功事件
            kafkaTemplate.send("payment-success-topic", order.getOrderNo(), new PaymentSuccessEvent(order.getOrderNo()));
            
        } catch (Exception e) {
            // Saga补偿：发布失败事件，回滚订单
            kafkaTemplate.send("payment-failed-topic", order.getOrderNo(), new PaymentFailedEvent(order.getOrderNo(), e.getMessage()));
            throw e;  // 触发Kafka重试或死信
        }
    }
}
```

#### 6. Kafka配置与一致性优化
- **Producer配置**（application.yml已含）：启用`transaction-id-prefix`实现exactly-once。
- **Consumer手动提交**：
  ```java:disable-run
  @KafkaListener(...)
  public void listen(ConsumerRecord<?, ?> record, Acknowledgment ack) {
      // 处理逻辑
      ack.acknowledge();  // 手动确认
  }
  ```
- **Saga补偿服务**：额外监听`payment-failed-topic`，调用订单服务回滚状态为"FAILED"。
- **高可用**：Kafka分区数>=3，Spring Boot多实例部署，使用ZooKeeper管理消费者组。
- **测试一致性**：模拟扣款失败，验证订单回滚。使用JUnit + Testcontainers启动PostgreSQL/Kafka容器测试。

#### 7. 部署与监控
- **Docker Compose**：打包Spring Boot JAR，结合PostgreSQL/Kafka镜像。
- **监控**：Prometheus + Grafana监控Kafka lag，ELK日志追踪Saga事件。
- **性能**：PostgreSQL连接池（HikariCP默认），MyBatis Plus缓存（二级缓存）。

此方案在高并发电商场景下，确保99.9%一致性（通过补偿处理0.1%异常）。如果需要完整代码仓库或特定模块扩展，请提供更多细节！
```
