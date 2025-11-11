package xCloud.service;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 * @Description 适配 milvus V2.0
 * @Author Andy Fan
 * @Date 2025/11/10 16:16
 * @ClassName Embedding2Service
 */
@Service
@Slf4j
public class Embedding2Service {

    @Resource
    private MilvusClientV2 milvusClientV2;
    @Resource
    private TextVectorLogMapper textVectorLogMapper;

    @Resource
    private AliEmbeddingUtil aliEmbeddingUtil;

    @Value("${vector.dim}")
    private Integer dim;

    @Value("${vector.collection}")
    private String collectionName;

    /**
     * 创建集合（createCollection）
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

        // 添加 embedding 字段（FLOAT_VECTOR，1536 维）
        collectionSchema.addField(AddFieldReq.builder()
                .fieldName("vector")
                .dataType(DataType.FloatVector)
                .dimension(dim)
                .description("文本嵌入向量")
                .build());

        // 创建集合 Req
        CreateCollectionReq createReq = CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(collectionSchema)
                .build();

        // 执行创建,在 Milvus Java SDK（v2.x 版本）中，milvusClientV2.createCollection(createReq) 方法的返回类型为 void，即成功时不返回任何值。如果创建失败，它会抛出 MilvusClientException（或其子类）异常。因此，要判断创建是否成功，最直接的方式是捕获异常：
        try {
            milvusClientV2.createCollection(createReq);
            log.info("Collection 创建成功！");
        } catch (MilvusClientException e) {
            log.info("Collection 创建失败: " + e.getMessage());
            return "Collection 创建失败: " + e.getMessage();
        }
        // 创建索引（L2 度量在此指定）
        IndexParam indexParam = IndexParam.builder()
                .fieldName("vector")  // 字段名在此指定
                .indexName("idx")
                .indexType(IndexParam.IndexType.AUTOINDEX)  // 或 IndexType.IVF_FLAT 等
                .metricType(IndexParam.MetricType.L2)
//                cosine
//                .extraParams(Map.of("M", "16", "nlist", "128"))  // 具体索引参数（针对 IVF_FLAT 等；AUTOINDEX 可能无需或不同）
                .build();

        CreateIndexReq indexReq = CreateIndexReq.builder()
                .collectionName(collectionName)
                .indexParams(Collections.singletonList(indexParam))
                .build();
        milvusClientV2.createIndex(indexReq);

        // 加载集合到内存
        LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                .collectionName(collectionName)
                .build();
        milvusClientV2.loadCollection(loadCollectionReq);
        return "集合创建成功";
    }

    /**
     * 插入单个向量（insertVector）
     *
     * @param id   ID
     * @param text 在 Milvus 向量数据库的上下文中（基于之前的对话），insertVector(Long id, String text) 方法中的 id 参数的作用是作为主键（Primary Key），用于唯一标识插入的向量实体。具体来说：
     *             作用详解
     *             <p>
     *             唯一标识实体：Milvus 集合（Collection）通常定义一个主键字段（默认名为 "id" 或自定义），id 参数的值会填充到这个主键字段中，确保每个插入的向量实体（如嵌入的文本向量）具有唯一的 ID，便于后续查询、删除或更新。
     *             插入流程：
     *             id：作为实体的唯一标识符（Long 类型，支持整数主键）。
     *             text：可能是原始文本，会在插入前通过嵌入模型（如 BERT）转换为向量（vector），然后与 id 一起插入集合。
     *             <p>
     *             为什么需要 ID：
     *             Milvus 不自动生成 ID（除非配置为自增），所以手动提供 id 可以避免冲突，并支持精确检索（如 getById(id)）。
     *             如果集合已启用主键，插入时必须提供；否则会报错（e.g., "Primary key is required"）。
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> insertVector(Long id, String text) {
        try {
            VectorDTO embedding = aliEmbeddingUtil.embedding(text);
            id = CodeX.nextId();
            List<Double> vector = embedding.getVector();
            // 构建 JSON 数据
            JsonObject row = new JsonObject();
            Gson gson = new Gson();
            row.add("vector", gson.toJsonTree(vector));
            row.addProperty("id", id);
            InsertReq insertReq = InsertReq.builder()
                    .collectionName(collectionName)
                    .data(Collections.singletonList(row))
                    .build();
            InsertResp resp = milvusClientV2.insert(insertReq);
            log.info("插入ID: " + id);
            //log
            TextVectorLog textVectorLog = new TextVectorLog();
            textVectorLog.setId(id);
            textVectorLog.setText(text);
            textVectorLog.setVector(JSON.toJSONString(vector));
            textVectorLog.setCreate_time(LocalDateTime.now());
            textVectorLog.setSource("insertVector");
            textVectorLog.setRemark("System");
            int insert = textVectorLogMapper.insert(textVectorLog);
            if (insert > 0) {
                log.info("textVectorLog日志插入成功");
            } else {
                log.info("textVectorLog日志插入失败");
            }

            if (resp.getInsertCnt() == 1) {
                log.info("插入成功，ID: " + id);
                return Result.success(resp);
            } else {
                return Result.error("插入失败");
            }
        } catch (Exception e) {
            log.error("插入失败: " + e.getMessage() + e);
            return Result.error("插入失败: " + e.getMessage());
        }
    }

    /**
     * 批量插入向量（insertVectors）
     *
     * @param ids   2
     * @param texts 2
     */
    public void insertVectors(List<Long> ids, List<String> texts) {
        if (ids.size() != texts.size()) {
            throw new IllegalArgumentException("ID 和文本列表大小不匹配");
        }
        Gson gson = new Gson();
        JsonArray rows = new JsonArray();
        JsonObject row = new JsonObject();

        for (int i = 0; i < ids.size(); i++) {
            VectorDTO embedding = aliEmbeddingUtil.embedding(texts.get(i));
            List<Double> vector = embedding.getVector();


//            JsonArray embeddingArray = new JsonArray();
//            for (Double d : vector) {
//                embeddingArray.add(new JsonPrimitive(d));
//            }
            row.add("vector", gson.toJsonTree(vector));
//            row.addProperty("id", new JsonPrimitive(ids.get(i)));
            row.addProperty("id", CodeX.nextId());
            rows.add(row);
        }

        InsertReq insertReq = InsertReq.builder()
                .collectionName(collectionName)
                .data(Collections.singletonList(row))
                .build();

        InsertResp resp = milvusClientV2.insert(insertReq);
        if (resp.getInsertCnt() != 0) {
            throw new RuntimeException("批量插入失败: " + resp.getInsertCnt());
        }
        System.out.println("批量插入成功，共 " + resp.getInsertCnt() + " 条");
    }

    /**
     * 向量搜索（search）
     *
     * @param queryText queryText
     * @param topK      topK
     * @return List<Map < String, Object>>
     */
    public Result<Object> search(String queryText, int topK) {
        VectorDTO embedding = aliEmbeddingUtil.embedding(queryText);
        List<Double> queryVector = embedding.getVector();

        // 转换为 FloatVec（新 V2 需要）
        float[] queryArray = new float[queryVector.size()];
        for (int i = 0; i < queryVector.size(); i++) {
            queryArray[i] = queryVector.get(i).floatValue();
        }
        FloatVec floatVec = new FloatVec(queryArray);

        SearchReq searchReq = SearchReq.builder()
                .collectionName(collectionName)
                .data(List.of(floatVec))  // 单个查询向量
                .topK(topK)
//                .filter("id < 100")  // 可添加过滤表达式，如 "id > 10"
                .outputFields(Collections.singletonList("*"))  // 返回字段
//                .withMetricType()
                .build();

        SearchResp resp = milvusClientV2.search(searchReq);
        // 解析结果
        List<Map<String, Object>> results = new ArrayList<>();
        List<List<SearchResp.SearchResult>> searchResults = resp.getSearchResults();
        for (List<SearchResp.SearchResult> searchResult : searchResults) {
            for (SearchResp.SearchResult result : searchResult) {
                System.out.printf("ID: %d, Score: %f, %s\n", (long) result.getId(), result.getScore(), result.getEntity().toString());
                TextVectorLog textVectorLog = textVectorLogMapper.selectById((Long) result.getId());
                Map<String, Object> item = Map.of(
                        "id", result.getId(),
                        "distance", result.getScore(),
//                        "entity", result.getEntity(),
                        "content", Optional.ofNullable(textVectorLog).map(TextVectorLog::getText).orElse(""),
                        "primaryKey", result.getPrimaryKey()
                );
                results.add(item);
            }
        }
//        Map<String, Object> stringObjectMap = results.get(results.size() - 1);
//        stringObjectMap.get()

        return Result.success(results);
    }


    /**
     * 查询（query）
     */
    public Result<Object> query(Long id, String symbol) {
        try {
            QueryReq queryReq = QueryReq.builder()
                    .collectionName(collectionName)
                    .filter("id" + symbol + id)
                    .build();
            QueryResp queryResp = milvusClientV2.query(queryReq);
            if (queryResp == null || queryResp.getQueryResults().isEmpty()) {
                return Result.error("数据不存在");
            }
            long sessionTs = queryResp.getSessionTs();
            System.out.println("sessionTs: " + sessionTs);
            List<Long> ids = new ArrayList<>();
            for (QueryResp.QueryResult result : queryResp.getQueryResults()) {
                System.out.println(result.getEntity().get("id"));
                Long sss = (Long) result.getEntity().get("id");
                ids.add(sss);
                System.out.println(result.getEntity().get("vector"));
            }
            return Result.success(ids);
        } catch (Exception e) {
            log.error("query失败: " + e.getMessage() + e);
            return Result.error("query失败: " + e.getMessage());
        }
    }

    /**
     * 查询（query）, 大于
     *
     * @param id
     * @return
     */
    public Result<Object> queryBigger(Long id) {
        return query(id, " > ");
    }

    /**
     * 查询（query）, 等于
     */
    public Result<Object> queryEqual(Long id) {
        return query(id, " == ");
    }
}
