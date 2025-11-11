package xCloud.controller;


import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.CollectionInfo;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import xCloud.entity.Result;
import xCloud.entity.VectorEntity;
import xCloud.entity.dto.StockAllDTO;
import xCloud.entity.dto.StockRuleDTO;
import xCloud.entity.request.DropCollectionRequest;
import xCloud.entity.request.MilvusRequest;
import xCloud.entity.request.MilvusSearchReq;
import xCloud.service.Embedding2Service;
import xCloud.tools.CodeX;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/10 10:44
 * @ClassName MilvusTestController
 */
@Slf4j
@Tag(name = "Milvus Test", description = "Milvus 测试接口")
@RestController
public class MilvusController {


    @Resource
    private MilvusClientV2 milvusClientV2;

    @Resource
    private Embedding2Service embedding2Service;

    @Value("${vector.collection}")
    private String collectionName;

    /**
     * 0-测试 Milvus 连接
     *
     * @return String
     */
    @Operation(summary = "0 测试 Milvus 连接")
    @GetMapping("/test/milvus")
    public String testMilvus() {
        try {
            // 测试是否能连接到 Milvus
            boolean connected = milvusClientV2 != null;
            // 检查一个不存在的 Collection
            boolean has = milvusClientV2.hasCollection(
                    HasCollectionReq.builder()
                            .collectionName(collectionName)
                            .build()
            ).booleanValue();

            return "Milvus 连接成功！客户端可用: " + connected + ", Collection存在: " + has;
        } catch (Exception e) {
            return "Milvus 连接失败: " + e.getMessage();
        }
    }

    /**
     * 0-创建 Milvus 1024 Collection
     *
     * @return String
     */
    @PostMapping("/create-collection")
    @Operation(summary = "0 创建 Milvus 1024 Collection")
    public Result<String> createCollection() {
        embedding2Service.createCollection();
        return Result.success("Collection 创建成功");
    }

    /**
     * 1-插入 Milvus 数据
     *
     * @return String
     */
    @PostMapping("/milvus/insertVector")
    @Operation(summary = "1 插入 Milvus 数据")
    public Result<Object> insertVector(@Valid @RequestBody MilvusRequest request) {
        return embedding2Service.insertVector(CodeX.nextId(), request.getText());
    }
//如果必须保持同步返回（不推荐，高并发下退化
//    public Result<String> insertData() {
//        List<Double> vectors = embedding2Service.getEmbedding("樊迎宾").block();  // 阻塞获取（仅测试/低并发用）
//        // ... 其余逻辑同上
//        List<Long> longs = insertVectors(entities);
//        return Result.success(longs.toString());
//    }

    /**
     * 1-1-异步 插入 Milvus 数据
     *
     * @return String
     */
    @GetMapping("/milvus/insertAsync")
    @Operation(summary = "1-1 Async 插入 Milvus 数据")
    @PostMapping("/insert")
    public Mono<Result<String>> insertAsync() {
        embedding2Service.insertVector(CodeX.nextId(), "");
        return Mono.just(Result.success("插入成功"));
    }

    /**
     * 1.2-插入 Milvus 数据
     *
     * @return List<Long>
     */
    @PostMapping("/insert")
    @Operation(summary = "1-2 插入 Milvus 数据[随机]")
    public List<Long> insertVectors() {
        // 生成示例数据
        List<VectorEntity> entities = new ArrayList<>();
        Random random = new Random();

        for (long i = 1; i <= 10; i++) {
            VectorEntity entity = new VectorEntity();
            entity.setId(i);
            // 生成随机向量
            float[] vector = new float[128];
            for (int j = 0; j < 128; j++) {
                vector[j] = random.nextFloat();
            }
            entity.setVector(vector);

            entities.add(entity);
        }
        embedding2Service.insertVectors(null, null);
        return List.of(CodeX.nextId());
    }

    /**
     * 2 删除 Milvus Collection
     *
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/drop-collection")
    @Operation(summary = "2 删除 Milvus Collection")
    public Result<Object> dropCollection(@Valid @RequestBody DropCollectionRequest request) {
        try {
            milvusClientV2.dropCollection(DropCollectionReq.builder().collectionName(request.getName()).build());
            return Result.success("Collection 删除成功");
        } catch (Exception e) {
            log.error("Failed to drop Milvus collection: {}", e.getMessage(), e);
            return Result.error("Collection 删除失败 " + e.getMessage());
        }
    }

    /**
     * 4-搜索 Milvus 数据
     *
     * @return String
     */
    @PostMapping("/milvus/search")
    @Operation(summary = "4 搜索 Milvus 数据")
    public Result<Object> search(@Valid @RequestBody MilvusSearchReq request) {
        return Result.success(embedding2Service.search(request.getText(), 5));
    }

    /**
     * 4-1 搜索 Milvus 数据
     *
     * @return List<Long>
     */
    @GetMapping("/search")
    @Operation(summary = "4-1 搜索 Milvus 数据")
    public ResponseEntity<Object> searchVectors() {
        // 生成随机查询向量
        Random random = new Random();
        float[] queryVector = new float[128];
        for (int i = 0; i < 128; i++) {
            queryVector[i] = random.nextFloat();
        }

        // 搜索Top5相似向量
        return ResponseEntity.ok(embedding2Service.search("queryVector", 5));
    }

    /**
     * 4-2 查询 Milvus Collection 名称列表
     *
     * @return Result<List < String>>
     */
    @PostMapping("/collection-names")
    @Operation(summary = "4-2 查询 Milvus Collection 名称列表")
    public Result<List<String>> getCollectionNames() {
        try {
            ListCollectionsResp listCollectionsResp = milvusClientV2.listCollections();
            List<CollectionInfo> collectionInfos = listCollectionsResp.getCollectionInfos();
            List<String> names = collectionInfos.stream()
                    .map(CollectionInfo::getCollectionName)
                    .toList();

            return Result.success(names);
        } catch (Exception e) {
            log.error("Failed to get Milvus collection names: {}", e.getMessage(), e);
            return Result.error("Failed to retrieve collection names");
        }
    }

    /**
     * 4-3 query
     *
     * @return Result<Object>
     */
    @GetMapping("/queryBigger")
    @Operation(summary = "4-3 queryBigger")
    public Result<Object> queryBigger(@RequestParam("id") Long id) {
        try {
            return embedding2Service.queryBigger(id);
        } catch (Exception e) {
            log.error("Failed queryBigger Milvus collection: {}", e.getMessage(), e);
            return Result.error("Failed to queryBigger collection");
        }
    }

    /**
     * 4-4 queryEqual
     *
     * @return Result<Object>
     */
    @GetMapping("/queryEqual")
    @Operation(summary = "4-4 queryEqual")
    public Result<Object> queryEqual(@RequestParam("id") Long id) {
        try {
            return embedding2Service.queryEqual(id);
        } catch (Exception e) {
            log.error("Failed queryEqual Milvus collection: {}", e.getMessage(), e);
            return Result.error("Failed to queryEqual collection");
        }
    }

}
