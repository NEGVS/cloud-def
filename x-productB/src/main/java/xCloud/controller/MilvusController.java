package xCloud.controller;



import io.milvus.client.MilvusServiceClient;
import io.milvus.param.collection.HasCollectionParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.entity.VectorEntity;
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
@Tag(name = "Milvus Test", description = "Milvus 测试接口")
@RestController
public class MilvusController {

    @Resource
    private MilvusServiceClient milvusClient;
    @Resource
    private MilvusService milvusService;

    @Operation(summary = "测试 Milvus 连接")
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

    @GetMapping("/milvus/create")
    @Operation(summary = "创建 Milvus Collection")
    public String create() {
        return milvusService.createCollection();
    }

    @GetMapping("/milvus/insert")
    @Operation(summary = "插入 Milvus 数据")
    public String insert() {
        return milvusService.insertData();
    }

    @GetMapping("/milvus/search")
    @Operation(summary = "搜索 Milvus 数据")
    public String search() {
        return milvusService.searchVector();
    }

    //    222
    @PostMapping("/create-collection")
    @Operation(summary = "2 创建 Milvus Collection")
    public String createCollection() {
        milvusService.createCollection();
        return "Collection created successfully";
    }

    @PostMapping("/insert")
    @Operation(summary = "2 插入 Milvus 数据")
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

    @GetMapping("/search")
    @Operation(summary = "2 搜索 Milvus 数据")
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

    @DeleteMapping("/drop-collection")
    @Operation(summary = "2 删除 Milvus Collection")
    public String dropCollection() {
        milvusService.dropCollection();
        return "Collection dropped successfully";
    }

}
