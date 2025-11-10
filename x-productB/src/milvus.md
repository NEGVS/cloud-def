# Embedding
https://platform.baichuan-ai.com/docs/text-Embedding
sess...e52c
为向量字段设置索引参数
为了加速语义搜索，必须为向量字段创建索引。索引可以大大提高大规模向量数据的检索效率。
```java
  import io.milvus.v2.common.IndexParam;
  import java.util.*;
  
  List<IndexParam> indexes = new ArrayList<>();
  
  indexes.add(IndexParam.builder()
  .fieldName("dense_vector")
  .indexType(IndexParam.IndexType.AUTOINDEX)
  .metricType(IndexParam.MetricType.IP)
  .build());
```
在上面的示例中，使用AUTOINDEX 索引类型为dense_vector 字段创建了名为dense_vector_index 的索引。metric_type 设置为IP ，表示将使用内积作为距离度量。

Milvus 提供多种索引类型，以获得更好的向量搜索体验。AUTOINDEX 是一种特殊的索引类型，旨在平滑向量搜索的学习曲线。有很多索引类型可供您选择。详情请参阅 xxx。





//        List<List<Float>> vectors = Arrays.asList(randomVector(128), randomVector(128));
Mono<List<Float>> listMono = baichuanEmbeddingClient.embedText("樊迎宾");
log.info("--1--insertData--listMono: {}", JSON.toJSONString(listMono));
log.info("--2--insertData--doubleList: {}", JSON.toJSONString(listMono1));
List<Float> floats = embeddingService.generateEmbedding("樊迎宾");

# 下面是一个使用 **Spring Boot + Milvus** 的简单示例流程：
**目标**：

1. 插入 **10 句文本（自然语言句子）** 到 Milvus。
2. 将文本向量化（通过 Embedding 模型）。
3. 根据用户输入一句话，搜索 **最相似匹配的句子** 并返回。

---

## 实现步骤概览

| 步骤 | 内容                                            |
| -- | --------------------------------------------- |
| 1  | 创建 Milvus Collection（文本向量存储）                  |
| 2  | 使用 Embedding 模型将句子转换为向量（OpenAI / HuggingFace） |
| 3  | 插入 10 句文本到 Milvus                             |
| 4  | 搜索输入句子的 TopK 相似文本                             |
| 5  | 返回最匹配的句子                                      |

---

## 一、Collection 设计（字段）

| 字段名    | 类型                       |
| ------ | ------------------------ |
| id     | Int64 (自增主键)             |
| text   | VarChar（原始文本）            |
| vector | FloatVector (向量，维度如 768) |

---

## 二、示例句子（要插入的 10 句）

```text
"今天天气很好，适合出去散步。"
"我喜欢学习人工智能知识。"
"今晚想吃火锅。"
"你觉得电影《星际穿越》怎么样？"
"明天需要早起去上班。"
"我正在学习使用 Milvus 数据库。"
"春天是万物复苏的季节。"
"请推荐一本好看的小说。"
"跑步是一种很好的运动方式。"
"工作压力大时，我会听音乐放松。"
```

---

## 三、Spring Boot 核心代码结构

```plaintext
milvus-demo
├── MilvusConfig.java       // 配置 Milvus 客户端
├── MilvusService.java      // 负责建表、插入、搜索
├── EmbeddingService.java   // 调用 OpenAI/HuggingFace 生成向量
├── TestController.java     // 提供 REST API 测试
```

---

### 1️⃣ Milvus 客户端配置

```java
@Configuration
public class MilvusConfig {
    @Bean
    public MilvusClient milvusClient() {
        return new MilvusClient.Builder()
            .withHost("localhost")
            .withPort(19530)
            .build();
    }
}
```

---

### 2️⃣ 创建 Collection（如首次运行）

```java
@Service
public class MilvusService {

    private static final String COLLECTION_NAME = "sentence_demo";

    @Autowired
    private MilvusClient milvusClient;

    public void createCollection() {
        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
            .withCollectionName(COLLECTION_NAME)
            .withShardsNum(1)
            .addFieldSchema(FieldType.newBuilder()
                .withName("id").withDataType(DataType.Int64).withPrimaryKey(true).withAutoID(true).build())
            .addFieldSchema(FieldType.newBuilder()
                .withName("text").withDataType(DataType.VarChar).withMaxLength(200).build())
            .addFieldSchema(FieldType.newBuilder()
                .withName("vector").withDataType(DataType.FloatVector).withDimension(768).build())
            .build();

        milvusClient.createCollection(createParam);
    }
}
```

---

### 3️⃣ 插入文本 + 向量化

```java
public void insertSentence(String text, List<Float> vector) {
    List<InsertParam.Field> fields = new ArrayList<>();
    fields.add(new InsertParam.Field("text", Collections.singletonList(text)));
    fields.add(new InsertParam.Field("vector", Collections.singletonList(vector)));

    InsertParam insertParam = InsertParam.newBuilder()
        .withCollectionName(COLLECTION_NAME)
        .withFields(fields)
        .build();

    milvusClient.insert(insertParam);
}
```

---

### 4️⃣ 搜索最匹配句子

```java
public List<String> searchSimilar(String queryText) {
    List<Float> queryVector = embeddingService.toVector(queryText);

    SearchParam searchParam = SearchParam.newBuilder()
        .withCollectionName(COLLECTION_NAME)
        .withTopK(3)
        .withVectors(Collections.singletonList(queryVector))
        .withVectorFieldName("vector")
        .build();

    SearchResp resp = milvusClient.search(searchParam);
    // 解析搜索结果，返回 text 字段
}
```

---

## ✅ 示例：测试 REST API

```java
@RestController
@RequestMapping("/milvus")
public class TestController {

    @Autowired
    private MilvusService milvusService;

    @PostMapping("/search")
    public String search(@RequestParam String query) {
        List<String> result = milvusService.searchSimilar(query);
        return result.get(0); // 最匹配的一句
    }
}
```

---

## 🟢 最终效果

输入：

> "我想学数据库向量搜索"

返回最相似：

> "我正在学习使用 Milvus 数据库。"

---

# 需要我给你一个完整可直接运行的 **Spring Boot Demo 项目（含 pom.xml）** 吗？

**包含：**

* ✅ Milvus + REST API + Embedding mock
* ✅ 插入 10 句话
* ✅ 搜索接口

只要你说：`要完整项目` 我立刻打包给你。

-----open ai---
### Spring Boot 使用 Milvus 实现插入 10 句话并搜索最匹配句子的示例

以下是一个完整的 Spring Boot 项目示例，使用 Spring AI 集成 Milvus 向量数据库。流程如下：
- 使用 OpenAI 的嵌入模型（Embedding Model）将文本转换为向量。
- 将 10 句示例句子（作为 `Document`）插入 Milvus 集合中。
- 针对一个查询句子进行相似度搜索，返回最匹配的句子（topK=1）。

**前提条件**：
- Milvus 服务运行中（默认 localhost:19530，用户名 root，密码 Milvus）。
- OpenAI API Key（在 `application.yml` 中配置）。
- Maven 或 Gradle 构建工具。

#### 1. 项目依赖（pom.xml）
使用 Maven 添加以下依赖（Spring Boot 3.x 版本）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>milvus-springboot-example</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>milvus-springboot-example</name>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version> <!-- 或最新版本 -->
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0-M1</spring-ai.version> <!-- Spring AI 版本，根据最新调整 -->
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- Spring AI OpenAI Embedding -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-openai</artifactId>
            <version>${spring-ai.version}</version>
        </dependency>
        <!-- Spring AI Milvus Vector Store -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-vector-store-milvus</artifactId>
            <version>${spring-ai.version}</version>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 2. 配置文件（application.yml）
在 `src/main/resources/application.yml` 中配置：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:your-openai-api-key-here}  # 替换为你的 OpenAI API Key
      embedding:
        options:
          model: text-embedding-ada-002  # 使用 ada-002 模型，维度 1536
    vectorstore:
      milvus:
        client:
          host: localhost
          port: 19530
          username: root
          password: Milvus
        databaseName: default
        collectionName: sentence_collection  # 集合名称
        embeddingDimension: 1536  # 与嵌入模型维度匹配
        indexType: IVF_FLAT
        metricType: COSINE  # 余弦相似度
        initialize-schema: true  # 自动初始化 schema
```

#### 3. 主应用类（MilvusSpringbootExampleApplication.java）
```java
package com.example.milvusexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MilvusSpringbootExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(MilvusSpringbootExampleApplication.class, args);
    }
}
```

#### 4. 服务类（SentenceService.java）
在 `src/main/java/com/example/milvusexample/service` 包下创建，实现插入和搜索逻辑。

```java
package com.example.milvusexample.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class SentenceService {

    @Autowired
    private VectorStore vectorStore;  // Milvus VectorStore

    // 示例：10 句简单句子（可替换为中文句子）
    private final List<Document> sentences = List.of(
            new Document("The quick brown fox jumps over the lazy dog.", Map.of("id", "1")),
            new Document("Spring Boot is a framework for Java.", Map.of("id", "2")),
            new Document("Milvus is a vector database for similarity search.", Map.of("id", "3")),
            new Document("Artificial intelligence is changing the world.", Map.of("id", "4")),
            new Document("Learning programming is fun and rewarding.", Map.of("id", "5")),
            new Document("Coffee is the best way to start the day.", Map.of("id", "6")),
            new Document("Books open doors to new worlds.", Map.of("id", "7")),
            new Document("Exercise keeps the body healthy.", Map.of("id", "8")),
            new Document("Music soothes the soul.", Map.of("id", "9")),
            new Document("Travel broadens the mind.", Map.of("id", "10"))
    );

    @PostConstruct
    public void init() {
        // 启动时插入 10 句
        System.out.println("插入 10 句句子到 Milvus...");
        vectorStore.add(sentences);
        System.out.println("插入完成！");
    }

    public String searchMostSimilar(String query) {
        // 搜索最匹配的句子（topK=1）
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(1)  // 只返回最匹配的一个
                        .build()
        );
        if (!results.isEmpty()) {
            return results.get(0).getContent();  // 返回最匹配句子的内容
        }
        return "未找到匹配句子";
    }
}
```

#### 5. 控制器类（SentenceController.java）
在 `src/main/java/com/example/milvusexample/controller` 包下创建 REST 接口，便于测试。

```java
package com.example.milvusexample.controller;

import com.example.milvusexample.service.SentenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentenceController {

    @Autowired
    private SentenceService sentenceService;

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "What is Milvus?") String query) {
        String result = sentenceService.searchMostSimilar(query);
        return "查询: " + query + "\n最匹配句子: " + result;
    }
}
```

#### 运行和测试
1. 运行 `mvn spring-boot:run` 启动应用（确保 Milvus 和 OpenAI API 可用）。
2. 应用启动后，会自动插入 10 句句子。
3. 测试搜索：访问 `http://localhost:8080/search?query=vector%20database`（URL 编码）。
    - 示例输出：`查询: vector database 最匹配句子: Milvus is a vector database for similarity search.`

#### 注意事项
- **嵌入维度**：OpenAI ada-002 为 1536 维，确保 Milvus 配置匹配。
- **中文支持**：示例用英文句子，如需中文，可替换 `sentences` 列表（如 "Milvus 是一个向量数据库"），OpenAI 支持多语言。
- **错误处理**：生产环境添加异常处理。
- **免费替代**：若无 OpenAI Key，可用 Hugging Face 嵌入模型替换（添加相应 starter）。
- 参考文档：

此示例简单高效，如需扩展（如批量搜索、过滤），可进一步修改。

----openai---2--

### 在现有 POM 中集成 OpenAI Embedding Model

基于您现有的 POM 依赖（OkHttp3、Hugging Face Tokenizers 和 Milvus SDK），您可以使用 OpenAI 的官方 Java 客户端库来调用 OpenAI API 生成文本嵌入（Embedding）。这个客户端会自动处理 HTTP 请求（它依赖 OkHttp，因此与您现有的 OkHttp3 兼容）。

#### 1. 添加 OpenAI Java 客户端依赖
在您的 `pom.xml` 的 `<dependencies>` 部分添加以下依赖（最新版本为 4.3.0）：

```xml
<dependency>
    <groupId>com.openai</groupId>
    <artifactId>openai-java</artifactId>
    <version>4.3.0</version>
</dependency>
```

- **为什么这个库？** 它是 OpenAI 官方提供的 Java SDK，支持同步/异步调用 REST API，包括嵌入模型（如 `text-embedding-3-small` 或 `text-embedding-ada-002`）。它简化了 API 调用，而无需手动构建 HTTP 请求。
- 更新后运行 `mvn clean install` 以拉取依赖。

#### 2. 配置 OpenAI API Key
- 在 `application.yml` 或 `application.properties` 中添加 API Key（从 OpenAI 平台获取）：
  ```yaml
  openai:
    api-key: ${OPENAI_API_KEY:your-openai-api-key-here}  # 通过环境变量或直接替换
  ```
- 或者使用系统环境变量 `OPENAI_API_KEY`。

#### 3. 代码示例：使用 OpenAI Embedding Model 生成向量
创建一个服务类（例如 `EmbeddingService.java`）来处理文本向量化。假设您使用 Spring Boot（基于之前的上下文），注入配置。

```java
package com.example.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OkHttpOpenAIClient;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingRequest;
import com.openai.models.embeddings.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${openai.api-key}")
    private String apiKey;

    private OpenAIClient client;

    @PostConstruct
    public void init() {
        // 初始化 OpenAI 客户端，使用 OkHttp（与您的依赖兼容）
        client = OkHttpOpenAIClient.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * 将单个文本转换为嵌入向量
     * @param text 输入文本
     * @return 浮点数组向量（维度取决于模型，例如 ada-002 为 1536 维）
     */
    public float[] generateEmbedding(String text) {
        // 创建嵌入请求
        EmbeddingRequest request = EmbeddingRequest.builder()
                .input(text)  // 支持单个字符串或 List<String>
                .model("text-embedding-ada-002")  // 推荐模型，维度 1536；或 "text-embedding-3-small" (维度 1536)
                .build();

        try {
            // 同步调用 API
            EmbeddingResponse response = client.embeddings().create(request);
            List<Embedding> embeddings = response.getData();
            if (!embeddings.isEmpty()) {
                return embeddings.get(0).getEmbedding().getData();  // 返回 float[] 向量
            }
        } catch (Exception e) {
            System.err.println("生成嵌入失败: " + e.getMessage());
        }
        return new float[0];  // 错误时返回空向量
    }

    /**
     * 批量生成嵌入（示例：10 句文本）
     * @param texts 文本列表
     * @return Map<String, float[]> 文本到向量的映射
     */
    public Map<String, float[]> generateBatchEmbeddings(List<String> texts) {
        // 类似单个，但 input 为 List<String>
        EmbeddingRequest request = EmbeddingRequest.builder()
                .input(texts)
                .model("text-embedding-ada-002")
                .build();

        try {
            EmbeddingResponse response = client.embeddings().create(request);
            List<Embedding> embeddings = response.getData();
            // 处理响应：embeddings.get(i).getEmbedding().getData() 对应 texts.get(i)
            // 这里简化为 Map，实际可根据索引映射
        } catch (Exception e) {
            System.err.println("批量生成嵌入失败: " + e.getMessage());
        }
        return Map.of();  // 简化返回，实际实现映射
    }
}
```

- **关键点**：
   - **模型选择**：`text-embedding-ada-002` 是经典模型，输出 1536 维向量。确保与 Milvus 集合的 `embeddingDimension` 匹配（在 Milvus 配置中设置 1536）。
   - **输入**：支持单个字符串或列表（批量最多 2048 个 token）。
   - **输出**：`float[]` 数组，直接用于 Milvus 插入（例如 `InsertParam.FieldData` 中的向量字段）。
   - **异常处理**：生产环境中添加重试和日志。
   - **异步支持**：如果需要，使用 `client.embeddings().createAsync(request)`。

#### 4. 与 Milvus 集成示例
在您的 Milvus 服务中调用上述方法，将向量插入集合：

```java
// 假设您有 Milvus 连接（使用 io.milvus.param.*）
import io.milvus.param.R;
import io.milvus.param.collection.InsertParam;
import io.milvus.param.collection.FieldType;
import io.milvus.grpc.DataType;

// 在插入逻辑中
String text = "示例句子";
float[] vector = embeddingService.generateEmbedding(text);

// 构建 Milvus 插入参数
List<InsertParam.FieldData> fieldsData = List.of(
    InsertParam.newFieldData()
        .withType(FieldType.create("text", DataType.VarChar))
        .withName("text")
        .withVarchars(List.of(text)),
    InsertParam.newFieldData()
        .withType(FieldType.create("vector", DataType.FloatVector, 1536))  // 维度 1536
        .withName("embedding")
        .withFloats(List.of(vector))  // 包装为 List<float[]>
);

// 执行插入
R<InsertParam> insertResult = yourMilvusConnection.insert("your_collection", fieldsData);
if (insertResult.getStatus() != R.Status.Success.getCode()) {
    System.err.println("插入失败");
}
```

#### 注意事项
- **费用**：OpenAI API 按 token 计费，嵌入模型较便宜（约 $0.0001/1K tokens）。
- **Hugging Face Tokenizers**：如果您想用本地模型替代 OpenAI（免费），可以结合 DJL（Deep Java Library）使用 Hugging Face 模型，但这会增加复杂性，且性能不如云 API。
- **测试**：在控制器中添加端点测试，例如 `/embed?text=hello` 返回向量数组。
- **文档**：完整 API 参考 OpenAI 平台。

此集成简单高效，与您的现有依赖无缝兼容。如果需要异步或更多自定义，参考官方 GitHub。
