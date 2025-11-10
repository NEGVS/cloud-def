package xCloud.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xCloud.entity.dto.VectorDTO;
import xCloud.openAiChatModel.ali.embedding.AliEmbeddingUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Description 适配 milvus V2.0
 * @Author Andy Fan
 * @Date 2025/11/10 16:16
 * @ClassName Embedding2Service
 */
@Service

public class Embedding2Service {

    @Resource
    private MilvusClientV2 milvusClientV2;

    @Resource
    private AliEmbeddingUtil aliEmbeddingUtil;

    @Value("${vector.dim}")
    private Integer dim;

    @Value("${vector.collection}")
    private String collectionName;

    /**
     * 创建集合（createCollection）
     */
    public void createCollection() {

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
                .fieldName("embedding")
                .dataType(DataType.FloatVector)
                .dimension(dim)
                .description("文本嵌入向量")
                .build());

        // 创建集合 Req
        CreateCollectionReq createReq = CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(collectionSchema)
                .build();

        // 执行创建
        milvusClientV2.createCollection(createReq);

        // 创建索引（L2 度量在此指定）
        IndexParam indexParam = IndexParam.builder()
                .fieldName("vector")  // 字段名在此指定
                .indexName("idx")
                .indexType(IndexParam.IndexType.AUTOINDEX)  // 或 IndexType.IVF_FLAT 等
                .metricType(IndexParam.MetricType.L2)
                .extraParams(Map.of("M", "16", "nlist", "128"))  // 具体索引参数（针对 IVF_FLAT 等；AUTOINDEX 可能无需或不同）
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
    }

    /**
     * 插入单个向量（insertVector）
     *
     * @param id
     * @param text
     */
    public void insertVector(Long id, String text) {

        VectorDTO embedding = aliEmbeddingUtil.embedding(text);

        List<Double> vector = embedding.getVector();
        // 构建 JSON 数据
        JsonObject row = new JsonObject();
//        row.add("id", new JsonPrimitive(id));
//        JsonArray embeddingArray = new JsonArray();
//        for (Double d : vector) {
//            embeddingArray.add(new JsonPrimitive(d));
//        }

        Gson gson = new Gson();
        row.add("vector", gson.toJsonTree(vector));
        row.addProperty("id", 0L);

//        JsonArray rows = new JsonArray();
//        rows.add(row);

        InsertReq insertReq = InsertReq.builder()
                .collectionName("embedding_collection")
                .data(Collections.singletonList(row))
                .build();

        InsertResp resp = milvusClientV2.insert(insertReq);

        System.out.println(resp.getPrimaryKeys());
        if (resp.getInsertCnt() != 0) {
            throw new RuntimeException("插入失败: " + resp.getInsertCnt());
        }
        System.out.println("插入成功，ID: " + id + "，插入数: " + resp.getInsertCnt());
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
            row.add("embedding", gson.toJsonTree(vector));
//            row.addProperty("id", new JsonPrimitive(ids.get(i)));
            row.addProperty("id", 0L);
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
    public List<Map<String, Object>> search(String queryText, int topK) {
        VectorDTO embedding = aliEmbeddingUtil.embedding(queryText);
        List<Double> queryVector = embedding.getVector();

        // 转换为 FloatVec（新 V2 需要）
        float[] queryArray = new float[queryVector.size()];
        for (int i = 0; i < queryVector.size(); i++) {
            queryArray[i] = queryVector.get(i).floatValue();
        }
        FloatVec floatVec = new FloatVec(queryArray);

        SearchReq searchReq = SearchReq.builder()
                .collectionName("embedding_collection")
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
                Map<String, Object> item = Map.of(
                        "id", result.getId(),
                        "distance", result.getScore(),
                        "entity", result.getEntity(),
                        "primaryKey", result.getPrimaryKey()
                );
                results.add(item);
            }
        }

        return results;
    }
}
