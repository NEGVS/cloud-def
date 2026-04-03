package xCloud.service;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.exception.MilvusClientException;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadPoolExecutor;
import xCloud.entity.Result;
import xCloud.entity.TextVectorLog;
import xCloud.entity.dto.VectorDTO;
import xCloud.mapper.TextVectorLogMapper;
import xCloud.openAiChatModel.ali.embedding.AliEmbeddingUtil;
import xCloud.tools.CodeX;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Description Milvus 向量数据库操作 Service，适配 Milvus SDK V2
 * @Author Andy Fan
 * @Date 2025/11/10 16:16
 */
@Service
@Slf4j
public class Embedding2Service {

    /** 通用异步线程池，用于日志写入等非关键 IO 操作 */
    @Resource
    @Qualifier("taskExecutor")
    private ThreadPoolExecutor taskExecutor;

    /** Milvus V2 客户端，懒加载避免启动时连接超时阻断 */
    @Lazy
    @Resource
    private MilvusClientV2 milvusClientV2;

    /** 向量日志 Mapper，用于记录插入的文本和向量 */
    @Resource
    private TextVectorLogMapper textVectorLogMapper;

    /** 阿里云 DashScope Embedding 工具类 */
    @Resource
    private AliEmbeddingUtil aliEmbeddingUtil;

    /** 向量维度，从配置读取（需与 Embedding 模型输出维度一致） */
    @Value("${vector.dim}")
    private Integer dim;

    /** Milvus Collection 名称，从配置读取 */
    @Value("${vector.collection}")
    private String collectionName;

    // ================================
    // 0. Collection 管理
    // ================================

    /**
     * 创建 Collection（含 Schema、索引、加载到内存）
     * <p>
     * Schema 结构：
     * - id：INT64，主键，非自增
     * - vector：FLOAT_VECTOR，维度由配置决定
     * - text：VARCHAR，存储原始文本（可选，用于回显）
     *
     * @return 创建结果描述
     */
    public String createCollection() {

        // 使用 CollectionSchema 初始化（v2.5.x+ 风格）
        CreateCollectionReq.CollectionSchema collectionSchema = milvusClientV2.createSchema();

        // 添加 ID 字段（INT64，主键，非自增）
        collectionSchema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(false)
                .description("主键 ID")
                .build());

        // 向量字段：vector（FLOAT_VECTOR）
        collectionSchema.addField(AddFieldReq.builder()
                .fieldName("vector")
                .dataType(DataType.FloatVector)
                .dimension(dim)
                .description("文本 Embedding 向量")
                .build());

        // 创建 Collection
        CreateCollectionReq createReq = CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(collectionSchema)
                .build();

        try {
            milvusClientV2.createCollection(createReq);
            log.info("Collection [{}] 创建成功", collectionName);
        } catch (MilvusClientException e) {
            log.error("Collection [{}] 创建失败: {}", collectionName, e.getMessage(), e);
            return "Collection 创建失败: " + e.getMessage();
        }

        // 创建向量索引（AUTOINDEX + L2 距离）
        IndexParam indexParam = IndexParam.builder()
                .fieldName("vector")
                .indexName("idx_vector")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.L2)
                .build();

        milvusClientV2.createIndex(CreateIndexReq.builder()
                .collectionName(collectionName)
                .indexParams(Collections.singletonList(indexParam))
                .build());

        // 加载 Collection 到内存，使其可被搜索
        milvusClientV2.loadCollection(LoadCollectionReq.builder()
                .collectionName(collectionName)
                .build());

        log.info("Collection [{}] 索引创建并加载完成", collectionName);
        return "集合创建成功";
    }

    // ================================
    // 1. 插入
    // ================================

    /**
     * 插入单条文本向量
     * <p>
     * 流程：文本 → Embedding → 写入 Milvus + 记录日志到 MySQL
     *
     * @param text 原始文本（不可为空）
     * @return 插入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> insertVector(String text) {
        try {
            // 1. 文本转向量
            VectorDTO embedding = aliEmbeddingUtil.embedding(text);
            List<Double> vector = embedding.getVector();

            // 2. 生成唯一 ID
            long id = CodeX.nextId();

            // 3. 构建插入数据
            Gson gson = new Gson();
            JsonObject row = new JsonObject();
            row.addProperty("id", id);
            row.add("vector", gson.toJsonTree(vector));

            InsertResp resp = milvusClientV2.insert(InsertReq.builder()
                    .collectionName(collectionName)
                    .data(Collections.singletonList(row))
                    .build());

            // 4. 异步写入日志到 MySQL（不阻塞主流程）
            final long finalId = id;
            final List<Double> finalVector = vector;
            taskExecutor.execute(() -> {
                try {
                    TextVectorLog logEntry = new TextVectorLog();
                    logEntry.setId(finalId);
                    logEntry.setText(text);
                    logEntry.setVector(JSON.toJSONString(finalVector));
                    logEntry.setCreate_time(LocalDateTime.now());
                    logEntry.setSource("insertVector");
                    logEntry.setRemark("System");
                    int rows = textVectorLogMapper.insert(logEntry);
                    log.info("向量日志异步写入{}, id={}", rows > 0 ? "成功" : "失败", finalId);
                } catch (Exception ex) {
                    log.error("向量日志异步写入失败, id={}: {}", finalId, ex.getMessage(), ex);
                }
            });

            // 5. 返回结果
            if (resp.getInsertCnt() == 1) {
                log.info("向量插入 Milvus 成功, id={}", id);
                return Result.success(resp);
            }
            return Result.error("向量插入 Milvus 失败");

        } catch (Exception e) {
            log.error("insertVector 失败: {}", e.getMessage(), e);
            return Result.error("插入失败: " + e.getMessage());
        }
    }

    /**
     * 批量插入文本向量
     * <p>
     * 每条文本单独调用 Embedding 接口转为向量后批量写入 Milvus，ID 由雪花算法生成。
     *
     * @param texts 文本列表（不可为空）
     */
    public void insertVectors(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("文本列表不能为空");
        }

        Gson gson = new Gson();
        List<JsonObject> rows = new ArrayList<>();
        // 同时收集日志数据，Milvus 写入后异步落库
        List<TextVectorLog> logEntries = new ArrayList<>();

        // 逐条转向量，构建插入数据
        for (String text : texts) {
            VectorDTO embedding = aliEmbeddingUtil.embedding(text);
            List<Double> vector = embedding.getVector();
            long id = CodeX.nextId();

            JsonObject row = new JsonObject();
            row.addProperty("id", id);
            row.add("vector", gson.toJsonTree(vector));
            rows.add(row);

            TextVectorLog logEntry = new TextVectorLog();
            logEntry.setId(id);
            logEntry.setText(text);
            logEntry.setVector(JSON.toJSONString(vector));
            logEntry.setCreate_time(LocalDateTime.now());
            logEntry.setSource("insertVectors");
            logEntry.setRemark("System");
            logEntries.add(logEntry);
        }

        InsertResp resp = milvusClientV2.insert(InsertReq.builder()
                .collectionName(collectionName)
                .data(rows)
                .build());

        // 插入成功时 insertCnt == 实际插入条数
        if (resp.getInsertCnt() != texts.size()) {
            throw new RuntimeException("批量插入不完整，期望=" + texts.size() + "，实际=" + resp.getInsertCnt());
        }
        log.info("批量插入成功，共 {} 条", resp.getInsertCnt());

        // 异步批量写入日志到 MySQL
        taskExecutor.execute(() -> {
            try {
                textVectorLogMapper.insertBatch(logEntries);
                log.info("批量向量日志异步写入成功，共 {} 条", logEntries.size());
            } catch (Exception ex) {
                log.error("批量向量日志异步写入失败: {}", ex.getMessage(), ex);
            }
        });
    }

    // ================================
    // 4. 搜索 / 查询
    // ================================

    /**
     * 向量相似度搜索
     * <p>
     * 将查询文本转为向量后，在 Milvus 中检索最相似的 topK 条记录，
     * 并回查 MySQL 日志表补充原始文本内容。
     *
     * @param queryText 查询文本
     * @param topK      返回最相似结果数量
     * @return 结果列表，每条包含 id、distance、content、primaryKey
     */
    public Result<Object> search(String queryText, int topK) {
        // 1. 查询文本转向量
        VectorDTO embedding = aliEmbeddingUtil.embedding(queryText);
        List<Double> queryVector = embedding.getVector();

        // 2. Double[] → float[]（Milvus FloatVec 需要 float[]）
        float[] queryArray = new float[queryVector.size()];
        for (int i = 0; i < queryVector.size(); i++) {
            queryArray[i] = queryVector.get(i).floatValue();
        }

        // 3. 执行搜索
        SearchResp resp = milvusClientV2.search(SearchReq.builder()
                .collectionName(collectionName)
                .data(List.of(new FloatVec(queryArray)))
                .topK(topK)
                .outputFields(Collections.singletonList("*"))
                .build());

        // 4. 解析结果，回查 MySQL 补充文本内容
        List<Map<String, Object>> results = new ArrayList<>();
        for (List<SearchResp.SearchResult> group : resp.getSearchResults()) {
            for (SearchResp.SearchResult result : group) {
                log.info("搜索结果 id={}, score={}", result.getId(), result.getScore());
                TextVectorLog logEntry = textVectorLogMapper.selectById((Long) result.getId());
                results.add(Map.of(
                        "id", result.getId(),
                        "distance", result.getScore(),
                        "content", Optional.ofNullable(logEntry).map(TextVectorLog::getText).orElse(""),
                        "primaryKey", result.getPrimaryKey()
                ));
            }
        }
        return Result.success(results);
    }

    /**
     * 条件查询（内部通用方法）
     *
     * @param id     查询的 ID 值
     * @param symbol 比较符，如 " > " 或 " == "
     * @return 满足条件的 id 列表
     */
    private Result<Object> query(Long id, String symbol) {
        try {
            QueryResp queryResp = milvusClientV2.query(QueryReq.builder()
                    .collectionName(collectionName)
                    .filter("id" + symbol + id)
                    .build());

            if (queryResp == null || queryResp.getQueryResults().isEmpty()) {
                return Result.error("未查询到数据");
            }

            List<Long> ids = new ArrayList<>();
            for (QueryResp.QueryResult result : queryResp.getQueryResults()) {
                ids.add((Long) result.getEntity().get("id"));
            }
            log.info("query 结果数量: {}", ids.size());
            return Result.success(ids);

        } catch (Exception e) {
            log.error("query 失败: {}", e.getMessage(), e);
            return Result.error("query 失败: " + e.getMessage());
        }
    }

    /**
     * 查询 id 大于指定值的记录
     *
     * @param id 基准 ID
     */
    public Result<Object> queryBigger(Long id) {
        return query(id, " > ");
    }

    /**
     * 查询 id 等于指定值的记录
     *
     * @param id 目标 ID
     */
    public Result<Object> queryEqual(Long id) {
        return query(id, " == ");
    }
}
