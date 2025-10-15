最佳实践：创建 DTO 来发送而不是整个 Order 实体
public class OrderPaidEvent {
   private String orderNo;
   private BigDecimal amount;
   private String status;
}


# 启动命令：:单节点 Kafka(KRaft 模式）
命令1，
docker run -d \
--name kafka \
-p 9092:9092 \
--network=host \
-e KAFKA_CFG_PROCESS_ROLES=broker,controller \
-e KAFKA_CFG_NODE_ID=1 \
-e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
-e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
-e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
-e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
-e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
-e KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
bitnami/kafka


## 命令2，推荐，成功。
docker run -d \   --name kafka \   -p 9092:9092 \   -e KAFKA_CFG_PROCESS_ROLES=broker,controller \   -e KAFKA_CFG_NODE_ID=1 \   -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \   -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \   -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \   -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \   -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \   -e KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \   -e KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \   bitnami/kafka

参数说明
--name kafka：容器名称。
-p 9092:9092：映射 Broker 端口（9092 可外部访问，9093 不需映射，因为单节点 Controller 不对外）。
KAFKA_CFG_PROCESS_ROLES=broker,controller：指定角色，单节点同时担任 Broker 和 Controller。
KAFKA_CFG_NODE_ID=1：节点 ID，唯一标识此节点。
KAFKA_CFG_LISTENERS：定义监听地址，PLAINTEXT://:9092 用于 Broker，CONTROLLER://:9093 用于 KRaft。
KAFKA_CFG_ADVERTISED_LISTENERS：外部客户端访问地址，设为 localhost:9092（若需远程访问，改为主机 IP）。
KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP：映射协议，均使用 PLAINTEXT（无加密）。
KAFKA_CFG_CONTROLLER_LISTENER_NAMES：指定 Controller 的监听器名称。
KAFKA_CFG_CONTROLLER_QUORUM_VOTERS：定义 Controller 投票节点，单节点为 1@localhost:9093。
KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR=1：偏移量 Topic 副本数，单节点设为 1。
KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1：事务日志副本数，单节点设为 1。
KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true：自动创建 Topic，开发环境方便。

测试连接
nc -zv localhost 9092
预期输出：Connection to localhost 9092 port [tcp/*] succeeded!


检查容器内端口
如果仍需确认 Kafka 在容器内监听的端口：
进入容器

docker exec -it kafka bash
查看配置

cat /opt/bitnami/kafka/config/server.properties | grep listeners
预期输出：listeners=PLAINTEXT://:9092,CONTROLLER://:9093
检查监听

netstat -tuln | grep 9092
预期输出：tcp 0 0 0.0.0.0:9092 0.0.0.0:* LISTEN


## 查看版本

方法 1：通过容器内命令直接查询
步骤 1：进入 Kafka 容器
docker exec -it <kafka容器名或ID> /bin/bash
bitnami/kafka
docker exec -it bitnami/kafka /bin/bash
步骤 2：运行版本命令
# 查看 Kafka 服务端版本
kafka-topics.sh --version# 或直接查看 Jar 包版本ls /opt/kafka/libs/ | grep kafka_

3.5.1 (Commit: 2c0b73f9a0ad16b5)
说明：kafka-topics.sh --version 会返回 Kafka 的完整版本号。
方法 2：通过镜像标签确认
查看容器使用的镜像版本
docker inspect <kafka容器名或ID> | grep -i image

"Image": "bitnami/kafka:3.5.1"

通过镜像标签（最直接）--------------
1. 查看本地已下载的 Kafka 镜像
   docker images | grep kafka
   输出示例：
   bitnami/kafka          3.5.1          a1b2c3d4e5f6   2 weeks ago     1.2GB
   confluentinc/cp-kafka  7.4.0          x1y2z3w4v5u6   1 month ago     1.5GB
   版本信息：镜像标签（如 3.5.1、7.4.0）即为 Kafka 版本。
2. 若镜像标签为 latest 或无版本号
   运行临时容器并查询：
   docker run --rm bitnami/kafka:latest kafka-topics.sh --version


## 持续实时监控 Docker 容器（如 Kafka）的日志输出，可以使用以下命令实现循环查看：
docker logs --tail 100 -f kafka


# 1.1 添加 Kafka 依赖
Spring Boot 会自动引入兼容的 spring-kafka 和 kafka-clients。
<!-- Spring Boot Starter for Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <!--                <version>2.8.0</version>  &lt;!&ndash; 使用最新版本,可以不用指定，默认就有 &ndash;&gt;-->
</dependency>


## 2-配置 Kafka 连接
spring:
kafka:
#Kafka Broker 的地址列表，多个地址用逗号分隔
bootstrap-servers: localhost:9092
consumer:
#消费者组的 ID，同一个组的消费者会共同消费 Topic 中的数据，每个消息只会被组内的一个消费者消费
group-id: andy_group
#当消费者组中没有初始偏移量或当前偏移量在服务器上不再存在时，该如何处理。earliest 表示从最早的偏移量开始消费，latest 表示从最新的偏移量开始消费。
auto-offset-reset: earliest
#生产者用于序列化消息键和值的类。这里使用了字符串序列化器
key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
properties:
spring:
json:
trusted:
packages: xcloud.xproduct.domain
#        spring:
#        json.trusted.packages: *
#    不要使用*




### Raft 的核心概念：
Raft 是一种分布式一致性算法，用于在分布式系统中实现数据一致性和容错。它主要用于管理多个节点之间的日志复制，确保所有节点对系统的状态达成一致。Raft 的设计目标是简单易懂，相比其他一致性算法（如 Paxos），它更易于实现和教学。

1. **角色划分**：
   - **领导者（Leader）**：负责处理客户端请求、日志复制和心跳信号。
   - **跟随者（Follower）**：被动接收领导者的日志更新和心跳。
   - **候选者（Candidate）**：在领导者失效时，节点会成为候选者并发起选举。

2. **领导者选举**：
   - 当一个节点认为领导者不可用时，会进入候选者状态，请求其他节点投票。
   - 获得多数节点（quorum）投票的候选者成为新的领导者。

3. **日志复制**：
   - 领导者接收客户端的写请求，将其记录为日志条目，并复制到其他节点。
   - 一旦日志被大多数节点确认，领导者会提交日志并通知客户端。

4. **安全性**：
   - Raft 保证日志的顺序一致性，只有包含最新日志的节点才能成为领导者。
   - 通过任期（term）机制防止旧领导者干扰系统。

### Raft 的优点：
- **易于理解**：通过分解为领导者选举、日志复制和安全性等模块，逻辑清晰。
- **高可用性**：能容忍部分节点故障，只要多数节点正常工作，系统仍可运行。
- **广泛应用**：被用于分布式数据库（如 TiDB、etcd）和分布式存储系统。

如果你需要更详细的解释或具体实现方式，可以告诉我！
