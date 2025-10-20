package xCloud.controller;


import io.milvus.client.MilvusServiceClient;
import io.milvus.param.collection.HasCollectionParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import xCloud.entity.Result;
import xCloud.entity.VectorEntity;
import xCloud.entity.dto.StockAllDTO;
import xCloud.entity.dto.StockRuleDTO;
import xCloud.service.MilvusService;

import java.util.ArrayList;
import java.util.List;
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
    private MilvusServiceClient milvusClient;
    @Resource
    private MilvusService milvusService;


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
            boolean connected = milvusClient != null;
            // 检查一个不存在的 Collection
            boolean has = milvusClient.hasCollection(
                    HasCollectionParam.newBuilder()
                            .withCollectionName("test_collection")
                            .build()
            ).getData();

            return "Milvus 连接成功！客户端可用: " + connected + ", Collection存在: " + has;
        } catch (Exception e) {
            return "Milvus 连接失败: " + e.getMessage();
        }
    }

    /**
     * 0-创建 Milvus Collection
     *
     * @return String
     */
    @PostMapping("/create-collection")
    @Operation(summary = "0 创建 Milvus Collection")
    public Result<String> createCollection() {
        return milvusService.createCollection();
    }

    /**
     * 1-插入 Milvus 数据
     *
     * @return String
     */
    @GetMapping("/milvus/insert")
    @Operation(summary = "1 插入 Milvus 数据")
    public Result<String> insert() {
        return milvusService.insertData();
    }
//如果必须保持同步返回（不推荐，高并发下退化
//    public Result<String> insertData() {
//        List<Double> vectors = milvusService.getEmbedding("樊迎宾").block();  // 阻塞获取（仅测试/低并发用）
//        // ... 其余逻辑同上
//        List<Long> longs = insertVectors(entities);
//        return Result.success(longs.toString());
//    }

    /**
     * 1111-插入 Milvus 数据
     *
     * @return String
     */
    @GetMapping("/milvus/insertAsync")
    @Operation(summary = "1111Async 插入 Milvus 数据")
    @PostMapping("/insert")
    public Mono<Result<String>> insertAsync() {
        return milvusService.insertDataAsync();
    }

    /**
     * 1.1-插入 Milvus 数据
     *
     * @return List<Long>
     */
    @PostMapping("/insert")
    @Operation(summary = "1 插入 Milvus 数据[随机]")
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

        return milvusService.insertVectors(entities);
    }

    /**
     * 2 删除 Milvus Collection
     *
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/drop-collection")
    @Operation(summary = "2 删除 Milvus Collection")
    public ResponseEntity<Void> dropCollection() {
        try {
            milvusService.dropCollection();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to drop Milvus collection: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 4-搜索 Milvus 数据
     *
     * @return String
     */
    @GetMapping("/milvus/search")
    @Operation(summary = "4 搜索 Milvus 数据")
    public String search() {
        return milvusService.searchVector();
    }

    /**
     * 4 搜索 Milvus 数据
     *
     * @return List<Long>
     */
    @GetMapping("/search")
    @Operation(summary = "4 搜索 Milvus 数据")
    public List<Long> searchVectors() {
        // 生成随机查询向量
        Random random = new Random();
        float[] queryVector = new float[128];
        for (int i = 0; i < 128; i++) {
            queryVector[i] = random.nextFloat();
        }

        // 搜索Top5相似向量
        return milvusService.searchSimilarVectors(queryVector, 5);
    }

    /**
     * 4 查询 Milvus Collection 名称列表
     *
     * @return Result<List < String>>
     */
    @GetMapping("/collection-names")
    @Operation(summary = "4 查询 Milvus Collection 名称列表")
    public Result<List<String>> getCollectionNames() {
        try {
            List<String> names = milvusService.getCollectionNames();
            return Result.success(names);
        } catch (Exception e) {
            log.error("Failed to get Milvus collection names: {}", e.getMessage(), e);
            return Result.error("Failed to retrieve collection names");
        }
    }

    /**
     * 9996 -插入 Milvus 数据
     *
     * @return List<Long>
     */
    @PostMapping("/insertB")
    @Operation(summary = "999 StockRuleDTO")
    public List<Long> sssd(@RequestBody StockRuleDTO stockRuleDTO) {
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

        return milvusService.insertVectors(entities);
    }

    /**
     * 999 -插入 Milvus 数据
     *
     * @return List<Long>
     */
    @PostMapping("/insertBC")
    @Operation(summary = "999 StockAllDTO")
    public List<Long> sswsd(@RequestBody StockAllDTO stockAllDTO) {
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

        return milvusService.insertVectors(entities);
    }
}
