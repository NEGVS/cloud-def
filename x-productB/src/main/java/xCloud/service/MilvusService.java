package xCloud.service;

import com.alibaba.fastjson.JSON;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.FlushResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CollectionSchemaParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.FlushParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String COLLECTION_NAME = "test_vector_collection";

    /**
     * 1 创建 Collection
     * 创建一个简单的向量表，包含：
     * - id：主键
     * - vector：128 维向量
     */
    public String createCollection() {
        FieldType fieldType = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();
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
        return collection.getStatus().toString();
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
        Map<String, Object> indexParams = new HashMap<>();
        indexParams.put("nlist", 128); // 数字类型
        CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName("vector")
                .withIndexName("vector_idx")
                .withMetricType(MetricType.L2)
                .withIndexType(IndexType.IVF_FLAT)
//                .withParams(indexParams)
                .withExtraParam(JSON.toJSONString(indexParams))
                .build();
        milvusClient.createIndex(createIndexParam);

        // === 5 Load Collection 到内存用于查询 ===
        R<RpcStatus> rpcStatusR = milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(COLLECTION_NAME)
                        .build()
        );
        return insert.getStatus().toString();
    }

    // 3 搜索相似向量
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

    //4 生成随机向量
    private List<Float> randomVector(int dim) {
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < dim; i++) {
            vector.add((float) Math.random());
        }
        System.out.println(JSON.toJSONString(vector));
        return vector;
    }
}
