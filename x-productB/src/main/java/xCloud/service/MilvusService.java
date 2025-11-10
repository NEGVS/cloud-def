package xCloud.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.FlushResponse;
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
import io.milvus.param.highlevel.collection.ListCollectionsParam;
import io.milvus.param.highlevel.collection.response.ListCollectionsResponse;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.response.InsertResp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import xCloud.entity.Result;
import xCloud.entity.Sentence;
import xCloud.entity.VectorEntity;
import xCloud.entity.request.MilvusRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/10 11:50
 * @ClassName MilvusService
 */
@Slf4j
@Service
public class MilvusService {

    @Resource
    private MilvusServiceClient milvusClient;

    @Resource
    private BaichuanEmbeddingClient baichuanEmbeddingClient;

    @Resource
    private EmbeddingService embeddingService;

    // 集合名称
    @Value("${vector.collection}")
    private String COLLECTION_NAME;

    @Value("${vector.dim}")
    private int dim;

    private final List<String> sampleSentences = List.of(
            "今天天气很好，适合出去散步。",
            "我喜欢学习人工智能知识。",
            "今晚想吃火锅。",
            "你觉得电影《星际穿越》怎么样？",
            "明天需要早起去上班。",
            "我正在学习使用 Milvus 数据库。",
            "春天是万物复苏的季节。",
            "请推荐一本好看的小说。",
            "跑步是一种很好的运动方式。",
            "工作压力大时，我会听音乐放松。"
    );

    /**
     * 0 创建 Collection
     * 创建一个简单的向量表，包含：
     * - id：主键
     * - vector：1024 维向量
     */
    public Result<String> createCollection() {
        // 检查集合是否已存在
        HasCollectionParam hasParam = HasCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .build();
        R<Boolean> hasResult = milvusClient.hasCollection(hasParam);
        if (hasResult.getData() != null && hasResult.getData()) {
            return Result.success("集合已存在");
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
                .withDimension(dim)//1024 维向量
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
                .withExtraParam("{\"nlist\": 1024}")
                .build();
        R<RpcStatus> index = milvusClient.createIndex(indexParam);
        if (collection.getStatus() == 0) {

            return Result.success("集合创建成功");
        }

        return Result.error("集合创建失败");
    }


    /**
     * 1 insert 向量数据
     *
     * @return s
     */
    public List<Long> insertVectors(List<VectorEntity> entities) {
        log.info("\n--1--inserVectors--entities: {}", JSON.toJSONString(entities));
        List<Long> ids = entities.stream().map(VectorEntity::getId).collect(Collectors.toList());
        List<List<Float>> vectors = new ArrayList<>();
        for (VectorEntity entity : entities) {
            float[] arr = entity.getVector();
            if (ObjectUtil.isEmpty(arr)) {
                continue;
            }
            List<Float> vector = new ArrayList<>(arr.length);
            for (float v : arr) {
                vector.add(v);
            }
            vectors.add(vector);
        }
        if (vectors.isEmpty()) {
            log.info("\n--2--inserVectors--vectors is empty");
            return new ArrayList<>();
        }
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("vector", vectors));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(fields)
                .build();
        log.info("\n--2--insertVecors--insertParam: {}", JSON.toJSONString(insertParam));

        R<MutationResult> result = milvusClient.insert(insertParam);
        log.info("\n--3--insertVetors--result: {}", JSON.toJSONString(result));

        if (result == null || result.getData() == null) {
            System.out.println("\n\n\n\n插入失败\n\n\n\n");
            return new ArrayList<>();
        }

        // === 3 必须 Flush 才能检索 ===
        R<FlushResponse> flush = milvusClient.flush(
                FlushParam.newBuilder().withCollectionNames(Collections.singletonList(COLLECTION_NAME)).build()
        );
        log.info("\n--4--必须 Flush 才能检索: {}", JSON.toJSONString(flush));

        //=== 4 创建索引（必须）===
        // 准备 index 参数（Map）
//        Map<String, Object> indexParams = new HashMap<>();
//        indexParams.put("nlist", 1024); // 数字类型
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
        log.info("\n--5--Load Collection 到内存用于查询: {}", JSON.toJSONString(rpcStatusR));

        return result.getData().getIDs().getIntIdOrBuilder().getDataList();
    }

    /**
     * 1.1 异步插入向量数据
     *
     * @param entities
     * @return
     */
    public Mono<List<Long>> insertVectorsAsync(List<VectorEntity> entities) {
        try {
            Mono<List<Long>> listMono = Mono.fromCallable(() -> insertVectors(entities))
                    .subscribeOn(Schedulers.boundedElastic());
            log.info("\n--3--insertVectorsAsync--listMono: {}", JSONUtil.toJsonStr(listMono));
            return listMono;
        } catch (Exception e) {
            log.error("\n\nError inserting vectors: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    /**
     * 插入数据的响应式版本（优化后：拆分为明确步骤，每步独立处理；详细日志记录；全链路异常捕获与恢复）。
     * 假设 insert Vectors 是同步方法（内部可能调用异步 Milvus），若为响应式则替换为 flatMap。
     * 优化点：
     * - 拆分步骤：每个 map/doOnNext 对应一个逻辑阶段，便于调试。
     * - 日志：使用 SLF4J 记录每个步骤的输入/输出/耗时，便于追踪。
     * - 异常：每步添加 doOnError 日志，全链路 onErrorResume 提供降级（e.g., 返回失败结果），避免 NPE 等崩溃。
     * - 性能：subscribeOn 移到链路开头；添加 timeout 防阻塞。
     *
     * @return Mono<Result < String>>：异步返回插入结果（ID 列表字符串），失败时返回错误描述。
     * <p>
     * 优化说明
     * <p>
     * 拆分步骤：使用多个 .map 和 .doOnNext 明确划分 4 个核心阶段（获取 embedding → 构建实体 → 插入 → 构建结果），每个阶段独立，便于定位问题（e.g., 通过日志过滤 "步骤 X"）。
     * 一步一步执行：Reactor 链路天然异步，但通过 doOnSubscribe、doOnNext、doOnError 在每个节点记录执行点，确保"一步步"可追踪。使用 Mono.defer 延迟订阅，避免不必要的计算。
     * 详细日志：
     * <p>
     * SLF4J Logger：每个步骤记录输入/输出/耗时，使用 info（关键）、debug（细节）、warn（降级）、error（异常）。
     * 全局日志：起始/结束时间戳，总耗时，便于性能监控。
     * 异常上下文：日志中包含原因、输入大小、异常栈，便于复现（e.g., NPE 时打印实体数）。
     * <p>
     * <p>
     * 异常处理：
     * <p>
     * 每步捕获：doOnError 记录具体步骤异常；try-catch 在 sync 块（如 insert Vectors）防 NPE 传播。
     * 降级恢复：onErrorResume 在关键节点提供 fallback（e.g., 空向量/空 ID），返回 Result.failure 而非抛异常，确保上层（如 Controller）能优雅处理。
     * 全链路兜底：最终 onErrorResume 统一包装，避免未捕获异常导致 Servlet 崩溃。
     * 针对 Milvus NPE：在步骤 3 的 try-catch 中显式处理，建议 insert Vectors 内部也加 r.getStatus() 检查（参考前文）。
     */
    public Mono<Result<String>> insertDataAsync() {

        long startTime = System.currentTimeMillis(); // 全局起始时间，用于总耗时日志

        return Mono.defer(() -> {
            log.info("=== 步骤 0: 开始异步插入流程 (query: '樊迎宾') ===");
            return embeddingService.getEmbedding("樊迎宾")  // Mono<List<Double>>
                    .timeout(java.time.Duration.ofSeconds(30))  // 添加超时，防 embedding 服务卡住
                    .subscribeOn(Schedulers.boundedElastic())  // 全链路在 I/O 线程池，避免阻塞主线程
                    .doOnSubscribe(sub -> log.debug("步骤 0: 订阅 embedding 获取"))
                    .doOnNext(vectors -> {
                        log.info("=== 步骤 1: 获取 embedding 成功 (向量维度: {}, 耗时: {}ms) ===",
                                vectors != null ? vectors.size() : 0,
                                System.currentTimeMillis() - startTime);
                    })
                    .doOnError(err -> {
                        log.error("=== 步骤 1: 获取 embedding 失败 (原因: {}) ===", err.getMessage(), err);
                    })
                    .onErrorResume(err -> {
                        // 降级：返回空向量，避免全链路失败
                        log.warn("=== 步骤 1: embedding 失败，降级使用空向量 ===");
                        return Mono.just(new ArrayList<Double>());  // 或抛出自定义异常，根据业务
                    })
                    // === 步骤 2: 构建插入实体 ===
                    .map(vectors -> {
                        log.debug("步骤 2: 开始构建 VectorEntity (输入向量大小: {})", vectors.size());
                        List<VectorEntity> entities = new ArrayList<>();

                        try {
                            // 假设 vectors 是单个向量列表；若批量，添加循环
                            if (vectors != null && !vectors.isEmpty()) {
                                VectorEntity entity = new VectorEntity();
                                entity.setId(1L);  // 建议使用 UUID 或自增，避免硬码
                                entity.setVector(convertDoubleToFloatArray(vectors));  // 转换为 float[]
                                entities.add(entity);
                                log.debug("步骤 2: 构建实体成功 (实体数: {})", JSONUtil.toJsonStr(entities));
                            } else {
                                log.warn("步骤 2: 输入向量为空，实体列表为空");
                            }
                        } catch (Exception e) {
                            log.error("步骤 2: 构建实体时异常 (原因: {})", e.getMessage(), e);
                            throw new RuntimeException("构建 VectorEntity 失败", e);
                        }
                        return entities;
                    })
                    .doOnNext(entities -> {
                        log.info("=== 步骤 2: 构建实体完成 (实体数: {}) ===", entities.size());
                    })
                    .doOnError(err -> {
                        log.error("=== 步骤 2: 构建实体失败 (原因: {}) ===", err.getMessage(), safeMessage(err));
                    })
                    // === 步骤 3: 执行向量插入 ===
                    .flatMap(entities -> insertVectorsAsync(entities)
                            .doOnSuccess(ids -> log.info("Step3 insertVectorsAsync OK, ids={}", ids))
                            .onErrorResume(err -> {
                                log.error("Step3 insertVectorsAsync Failed, reason={}", safeMessage(err));
                                return Mono.just(Collections.emptyList());  // 降级，不抛异常
                            }))
                    .doOnNext(longs -> {
                        log.info("=== 步骤 3: 插入完成 (ID 列表: {}) ===", longs);
                    })
                    .doOnError(err -> {
                        log.error("=== 步骤 3: 插入失败 (原因: {}) ===", err.getMessage(), safeMessage(err));
                    })
                    .onErrorResume(err -> {
                        // 降级：返回空 ID 列表，避免全链路崩溃
                        log.warn("=== 步骤 3: 插入失败，降级返回空结果 ===");
                        return Mono.just(new ArrayList<Long>());
                    })
                    // === 步骤 4: 构建最终结果 ===
                    .map(longs -> {
                        log.debug("步骤 4: 构建结果 (ID 字符串: {})", longs.toString());
                        Result<String> result = Result.success(longs.toString());
                        log.info("=== 步骤 4: 结果构建完成 (成功: {}) ===", JSONUtil.toJsonStr(result));
                        return result;
                    })
                    .doOnSuccess(result -> {
                        long totalTime = System.currentTimeMillis() - startTime;
                        log.info("=== 全局: doOnSuccess 异步插入流程结束 (总耗时: {}ms, 结果: {}) ===", totalTime, result.getData());
                    })
                    .doOnError(err -> {
                        long totalTime = System.currentTimeMillis() - startTime;
                        log.error("=== 全局: doOnError 异步插入流程异常结束 (总耗时: {}ms, 原因: {}) ===", totalTime, err.getMessage(), err);
                    })
                    .onErrorResume(err -> {
                        // 最终兜底：包装为失败 Result，避免 Mono 抛异常到上层
                        return Mono.just(Result.error("插入数据失败: " + err.getMessage()));
                    });
        });
    }

    private String safeMessage(Throwable t) {
        if (t == null) {
            return "未知异常 (null)";
        }
        try {
            String msg = t.getMessage();
            return (msg != null && !msg.trim().isEmpty()) ? msg : t.toString();
        } catch (Exception e) {  // 捕获 getMessage() 抛出的任何异常（如 NPE）
            log.warn("safeGetErrorMessage: getMessage() 失败，回退到 toString(), 原因: {}", safeMessage(e), e);
            return t.toString();
        }
    }

    /**
     * 1 insert 向量数据
     *
     * @return s
     */
    public Result<String> insertVector(MilvusRequest request) {
        // === 1 构建插入数据 ===
        Mono<List<Double>> listMono1 = embeddingService.getEmbedding("樊迎宾");
        List<VectorEntity> entities = new ArrayList<>();
//        for (int i = 0; i < vectors.size(); i++) {
//            VectorEntity entity = new VectorEntity();
//            entity.setId((long) i + 1);
//            entity.setVector(convertToFloatArray(vectors.get(i)));
//            entities.add(entity);
//        }
        VectorEntity entity = new VectorEntity();
        entity.setId(1L);
//        entity.setVector(convertToFloatArray(floats));
        entities.add(entity);
        List<Long> longs = insertVectors(entities);
        return Result.success(longs.toString());
    }

    public void insertR() {
        List<JsonObject> rows = new ArrayList<>();
        Gson gson = new Gson();
        rows.add(gson.fromJson("{\"dense_vector\": [0.1, 0.2, 0.3, 0.4]}", JsonObject.class));
        rows.add(gson.fromJson("{\"dense_vector\": [0.2, 0.3, 0.4, 0.5]}", JsonObject.class));
        MilvusClientV2 client = new MilvusClientV2(ConnectConfig.builder()
                .uri("http://localhost:19530")
                .build());

    }


    /**
     * 2 删除集合
     */
    public void dropCollection() {
        DropCollectionParam dropParam = DropCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .build();
        milvusClient.dropCollection(dropParam);
    }


    /**
     * 4 搜索最匹配的句子
     */
    public List<Sentence> searchSimilarSentences(String queryText, int topK) {
//        float[] queryVector = vectorService.textToVector(queryText);

        List<String> outFields = new ArrayList<>();
        outFields.add("id");
        outFields.add("content");

//        SearchParam searchParam = SearchParam.newBuilder()
//                .withCollectionName(COLLECTION_NAME)
//                .withFieldName("vector")
//                .withQueryVectors(queryVector)
//                .withTopK(topK)
//                .withMetricType(MetricType.L2) // 使用欧氏距离
//                .withOutFields(outFields)
//                .withParams("{\"nprobe\": 10}")
//                .build();
//
//        SearchResponse response = milvusClient.search(searchParam);
//        if (response.getStatus() != R.Status.Success.getCode()) {
//            throw new RuntimeException("搜索失败: " + response.getMessage());
//        }
//
//        List<Sentence> result = new ArrayList<>();
//        for (SearchResponse.QueryResult queryResult : response.getQueryResults()) {
//            for (SearchResponse.SearchResultData resultData : queryResult.getResultList()) {
//                Long id = resultData.getFieldValue("id");
//                String content = resultData.getFieldValue("content");
//                result.add(new Sentence(id, content, null));
//            }
//        }

//        return result;
        return null;
    }

    /**
     * 4 搜索相似向量
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
     * 4- 查询集合名称列表
     * url: https://milvus.io/docs/zh/view-collections.md
     */
    public List<String> getCollectionNames() {
        try {
            ListCollectionsParam param = ListCollectionsParam.newBuilder().build();
            R<ListCollectionsResponse> resp = milvusClient.listCollections(param);
            log.info("\nListCollectionsResponse: {}", JSONUtil.toJsonStr(resp));

            if (resp.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException("Failed to list collections: " + resp.getMessage());
            }
            ListCollectionsResponse data = resp.getData();
            List<String> collectionNames = data.collectionNames;
            log.info("\nCollection names: {}", collectionNames);
            return collectionNames;
        } catch (Exception e) {
            log.error("Failed to get Milvus collection names: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve collection names", e);
        }
    }

    /**
     * 4 搜索相似向量
     *
     * @return s
     */
    public String searchVector() {
        List<Float> query = randomVector(1024);
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
     * 生成随机向量
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
     * 辅助方法：将 List<Float>转换为 float []
     */
    private float[] convertToFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            // 注意：如果 list 中存在 null 元素，这里会抛出 NullPointerException
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * 辅助方法：将 List<Double> 转换为 float []
     */
    private float[] convertDoubleToFloatArray(List<Double> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            // 注意：如果 list 中存在 null 元素，这里会抛出 NullPointerException
            array[i] = list.get(i).floatValue();  // Double 转换为 float
        }
        return array;
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
