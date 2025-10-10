package xCloud.service;

import com.alibaba.fastjson.JSON;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.FlushResponse;
import io.milvus.grpc.IDs;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResultData;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CollectionSchemaParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.FlushParam;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.FieldDataWrapper;
import io.milvus.response.SearchResultsWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xCloud.entity.VectorEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/10 11:50
 * @ClassName MilvusService
 */
@Service
public class MilvusService {
    @Resource
    private MilvusServiceClient milvusClient;
    // 集合名称
    private static final String COLLECTION_NAME = "test_vector_collection";
    // 向量维度
    private static final int VECTOR_DIM = 128;

    /**
     * 1 创建 Collection
     * 创建一个简单的向量表，包含：
     * - id：主键
     * - vector：128 维向量
     */
    public String createCollection() {
        // 检查集合是否已存在
        HasCollectionParam hasParam = HasCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .build();
        R<Boolean> hasResult = milvusClient.hasCollection(hasParam);
        if (hasResult.getData()) {
            return "ok";
        }
        // 定义字段
        FieldType fieldType = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();
        // 创建集合
        FieldType vectorField = FieldType.newBuilder()
                .withName("vector")
                .withDataType(DataType.FloatVector)
                .withDimension(128)//128 维向量
                .build();
        CollectionSchemaParam schema = CollectionSchemaParam.newBuilder()
                .addFieldType(fieldType)
                .addFieldType(vectorField)
                .build();

        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withDescription("Test collection")
                .withShardsNum(2)
                .withSchema(schema)
                .build();
        R<RpcStatus> collection = milvusClient.createCollection(createCollectionReq);
        // 创建索引
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName("vector")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.L2)
                .withExtraParam("{\"nlist\": 128}")
                .build();
        R<RpcStatus> index = milvusClient.createIndex(indexParam);
        return collection.getStatus().toString();
    }

    /**
     * 2.1 insert 向量数据
     *
     * @return s
     */
    public List<Long> insertVectors(List<VectorEntity> entities) { // 准备插入数据
        List<Long> ids = entities.stream().map(VectorEntity::getId).collect(Collectors.toList());
        List<List<Float>> vectors =new ArrayList<>();
        for (VectorEntity entity : entities) {
            float[] arr = entity.getVector();
            List<Float> vector = new ArrayList<>(arr.length);
            for (float v : arr) vector.add(v);
            vectors.add(vector);
        }
        List<InsertParam.Field> fields = new ArrayList<>();
//        fields.add(new InsertParam.Field("id", ids));
        fields.add(new InsertParam.Field("vector", vectors));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(fields)
                .build();

        R<MutationResult> result = milvusClient.insert(insertParam);
        if (result == null || result.getData() == null) {
            System.out.println("\n\n\n\n插入失败\n\n\n\n");
            return new ArrayList<>();
        }
        return result.getData().getIDs().getIntIdOrBuilder().getDataList();
    }

    /**
     * 2 insert 向量数据
     *
     * @return s
     */
    public String insertData() {
        // === 1 构建插入数据 ===
        List<List<Float>> vectors = Arrays.asList(randomVector(128), randomVector(128));

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("vector", vectors));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(fields)
                .build();
        // === 2 插入数据 ===
        R<MutationResult> insert = milvusClient.insert(insertParam);
        if (insert.getData() == null) {
            return insert.getData().getSuccIndexCount() + " vectors inserted successfully";
        }
        // === 3 必须 Flush 才能检索 ===
        R<FlushResponse> flush = milvusClient.flush(
                FlushParam.newBuilder().withCollectionNames(Collections.singletonList(COLLECTION_NAME)).build()
        );
        //=== 4 创建索引（必须）===
        // 准备 index 参数（Map）
//        Map<String, Object> indexParams = new HashMap<>();
//        indexParams.put("nlist", 128); // 数字类型
//        CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
//                .withCollectionName(COLLECTION_NAME)
//                .withFieldName("vector")
//                .withIndexName("vector_idx")
//                .withMetricType(MetricType.L2)
//                .withIndexType(IndexType.IVF_FLAT)
////                .withParams(indexParams)
//                .withExtraParam(JSON.toJSONString(indexParams))
//                .build();
//        milvusClient.createIndex(createIndexParam);

        // === 5 Load Collection 到内存用于查询 ===
        R<RpcStatus> rpcStatusR = milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(COLLECTION_NAME)
                        .build()
        );
        return insert.getStatus().toString();
    }

    /**
     * 3.1 搜索相似向量
     *
     * @return s
     */
    public List<Long> searchSimilarVectors(float[] queryVector, long topK) {
        if (queryVector == null || queryVector.length == 0) {
            throw new IllegalArgumentException("查询向量 queryVector 不能为空");
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("topK 必须大于 0");
        }
        // 加载集合
        LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .build();
        milvusClient.loadCollection(loadParam);

        // 构建搜索向量
        List<List<Float>> vectors = new ArrayList<>();
        vectors.add(convertToFloatList(queryVector));
        //准备搜索参数
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withVectorFieldName("vector") // 向量字段名
                .withMetricType(MetricType.L2) // L2 距离（可换为 IP / COSINE）
                .withFloatVectors(vectors)
                .withLimit(topK)// 返回前 K 个
                .withParams("{\"nprobe\": 10}")
                //Collections.singletonList("id")，Arrays.asList("id")
                .withOutFields(Collections.singletonList("id"))// 只返回 id 字段
                .build();

        // 执行搜索
        R<SearchResults> result = milvusClient.search(searchParam);

        if (result == null || result.getData() == null) {
            throw new RuntimeException("Milvus 搜索失败：" +
                    (result != null ? result.getException() : "未知错误"));
        }
        // 处理结果（封装为 ID 集合）
        List<Long> resultIds = new ArrayList<>();


        SearchResults results = result.getData();
//        List<SearchResults.SearchResult> searchResults = results.getResultsList();
        SearchResultData results1 = results.getResults();

        SearchResultsWrapper wrapper = new SearchResultsWrapper(result.getData().getResults());

        List<?> idList = wrapper.getFieldData("id", 0);
        for (Object obj : idList) {
            if (obj instanceof Long) {
                resultIds.add((Long) obj);
            } else if (obj instanceof Integer) {
                resultIds.add(((Integer) obj).longValue());
            } else {
                throw new RuntimeException("未知 ID 类型: " + obj.getClass());
            }
        }

//        FieldDataWrapper idWrapper = wrapper.getFieldWrapper("id");
////        List<?> fieldData = idWrapper.getFieldData();
//        wrapper.getFieldData().forEach((fieldName, fieldData) -> {
//            if ("id".equals(fieldName)) {
//                List<Long> ids = fieldData.getLongData();
//                topKIds.addAll(ids);
//            }
//        });
//
//
//        wrapper.getRowCount
//        IDs ids1 = results.getResults().getIds().getIdArray();
//
////        for (int i = 0; i < wrapper.getRowCount(); i++) {
////            resultIds.add(wrapper.getFieldLongValue(i, "id"));
////        }
//        // 3️⃣ 遍历 SearchResult（逐条结果）
//        for (SearchResults.SearchResult result : searchResults) {
//            List<SearchResults.IDs> idsList = result.getIdsList();
//
//            for (SearchResults.IDs ids : idsList) {
//                // 获取 long 类型 ID（注意类型区分：int_id / str_id）
//                for (Long id : ids.getLongIdsList()) {
//                    resultIds.add(id);
//                }
//            }
//        }
        System.out.println("搜索结果 IDs: " + resultIds);
        return resultIds;
    }

    /**
     * 3 搜索相似向量
     *
     * @return s
     */
    public String searchVector() {
        List<Float> query = randomVector(128);
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withVectorFieldName("vector")
                .withMetricType(MetricType.L2)
                .withLimit(3L)
                .withFloatVectors(Arrays.asList(query))
                .build();
        //在 Search 之前必须执行 loadCollection()
        milvusClient.loadCollection(
                LoadCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build()
        );
        R<SearchResults> search = milvusClient.search(searchParam);
        if (search.getData() == null) {
            System.err.println("Search failed: " + search.getStatus());
            return "Search failed: " + search.getStatus();
        }
        return search.getData().getResults().toString();
    }

    /**
     * 删除集合
     */
    public void dropCollection() {
        DropCollectionParam dropParam = DropCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .build();
        milvusClient.dropCollection(dropParam);
    }

    /**
     * 4 生成随机向量
     *
     * @param dim 向量维度
     * @return 向量
     */
    private List<Float> randomVector(int dim) {
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < dim; i++) {
            vector.add((float) Math.random());
        }
        System.out.println(JSON.toJSONString(vector));
        return vector;
    }

    /**
     * 辅助方法：将float[]转换为List<Float>
     */


    private List<Float> convertToFloatList(float[] array) {
        List<Float> list = new ArrayList<>(array.length);
        for (float value : array) {
            list.add(value);
        }
        return list;
    }
}
