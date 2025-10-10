package xCloud.controller;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/10 10:44
 * @ClassName MilvusTestController
 */

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.collection.HasCollectionParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.service.MilvusService;

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
}
