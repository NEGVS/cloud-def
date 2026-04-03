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
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import xCloud.entity.Result;
import xCloud.entity.request.DropCollectionRequest;
import xCloud.entity.request.MilvusRequest;
import xCloud.entity.request.MilvusSearchReq;
import xCloud.service.Embedding2Service;

import java.util.List;

/**
 * @Description Milvus 向量数据库管理接口
 *              包含 Collection 管理、向量插入、向量搜索、条件查询等功能
 * @Author Andy Fan
 * @Date 2025/10/10 10:44
 */
@Slf4j
@Tag(name = "Milvus", description = "Milvus 向量数据库接口")
@RestController
@RequestMapping("/milvus")
public class MilvusController {

    /** Milvus V2 客户端，懒加载避免启动时连接超时 */
    @Lazy
    @Resource
    private MilvusClientV2 milvusClientV2;

    /** 向量 Embedding + 搜索业务逻辑 */
    @Lazy
    @Resource
    private Embedding2Service embedding2Service;

    /** Collection 名称，从配置读取 */
    @Value("${vector.collection}")
    private String collectionName;

    // ================================
    // 0. 连接 & Collection 管理
    // ================================

    /**
     * 测试 Milvus 连接，并检查目标 Collection 是否存在
     */
    @Operation(summary = "测试 Milvus 连接")
    @GetMapping("/test")
    public String testMilvus() {
        try {
            boolean has = milvusClientV2.hasCollection(
                    HasCollectionReq.builder()
                            .collectionName(collectionName)
                            .build()
            ).booleanValue();
            return "Milvus 连接成功！Collection [" + collectionName + "] 存在: " + has;
        } catch (Exception e) {
            return "Milvus 连接失败: " + e.getMessage();
        }
    }

    /**
     * 创建 Collection（含 Schema、向量索引、加载到内存）
     * 已存在时 Milvus 会抛异常，接口层返回失败信息
     */
    @Operation(summary = "创建 Collection")
    @PostMapping("/collection/create")
    public Result<String> createCollection() {
        String result = embedding2Service.createCollection();
        return result.contains("失败") ? Result.error(result) : Result.success(result);
    }

    /**
     * 删除指定 Collection（不可逆，请谨慎调用）
     *
     * @param request 包含要删除的 Collection 名称
     */
    @Operation(summary = "删除 Collection")
    @DeleteMapping("/collection/drop")
    public Result<Object> dropCollection(@Valid @RequestBody DropCollectionRequest request) {
        try {
            milvusClientV2.dropCollection(
                    DropCollectionReq.builder().collectionName(request.getName()).build()
            );
            log.info("Collection [{}] 删除成功", request.getName());
            return Result.success("Collection 删除成功");
        } catch (Exception e) {
            log.error("Collection [{}] 删除失败: {}", request.getName(), e.getMessage(), e);
            return Result.error("Collection 删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有 Collection 名称列表
     */
    @Operation(summary = "查询所有 Collection 名称")
    @GetMapping("/collection/list")
    public Result<List<String>> getCollectionNames() {
        try {
            ListCollectionsResp resp = milvusClientV2.listCollections();
            List<String> names = resp.getCollectionInfos().stream()
                    .map(CollectionInfo::getCollectionName)
                    .toList();
            return Result.success(names);
        } catch (Exception e) {
            log.error("查询 Collection 列表失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    // ================================
    // 1. 插入
    // ================================

    /**
     * 插入单条文本向量
     * <p>
     * 文本经 Embedding 模型转为向量后写入 Milvus，同步记录日志到 MySQL。
     *
     * @param request 包含待插入的文本内容
     */
    @Operation(summary = "插入单条文本向量")
    @PostMapping("/vector/insert")
    public Result<Object> insertVector(@Valid @RequestBody MilvusRequest request) {
        return embedding2Service.insertVector(request.getText());
    }

    // ================================
    // 4. 搜索 & 查询
    // ================================

    /**
     * 向量相似度搜索（Top5）
     * <p>
     * 将查询文本转为向量，在 Milvus 中检索最相似的 5 条记录。
     *
     * @param request 包含查询文本
     */
    @Operation(summary = "向量相似度搜索 Top5")
    @PostMapping("/vector/search")
    public Result<Object> search(@Valid @RequestBody MilvusSearchReq request) {
        return embedding2Service.search(request.getText(), 5);
    }

    /**
     * 查询 id 大于指定值的记录
     *
     * @param id 基准 ID
     */
    @Operation(summary = "查询 id > 指定值的记录")
    @GetMapping("/query/bigger")
    public Result<Object> queryBigger(@RequestParam("id") Long id) {
        try {
            return embedding2Service.queryBigger(id);
        } catch (Exception e) {
            log.error("queryBigger 失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询 id 等于指定值的记录
     *
     * @param id 目标 ID
     */
    @Operation(summary = "查询 id == 指定值的记录")
    @GetMapping("/query/equal")
    public Result<Object> queryEqual(@RequestParam("id") Long id) {
        try {
            return embedding2Service.queryEqual(id);
        } catch (Exception e) {
            log.error("queryEqual 失败: {}", e.getMessage(), e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
