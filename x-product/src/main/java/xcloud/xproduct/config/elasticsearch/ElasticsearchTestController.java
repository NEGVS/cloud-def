package xcloud.xproduct.config.elasticsearch;

import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xcloud.xproduct.domain.XProducts;
import xcloud.xproduct.entity.ResultEntity;

import java.io.IOException;
import java.util.List;

/**
 * @Description x
 * @Author Andy Fan
 * @Date 2025/3/6 17:24
 * @ClassName ElasticsearchTestController
 */
@RestController
@Slf4j
@RequestMapping("/elastic")
@Tag(name = "Elasticsearch测试")
public class ElasticsearchTestController {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ElasticsearchQueryService elasticsearchQueryService;

    /**
     * 1-创建索引
     *
     * @param indexName x
     * @return x
     * @throws IOException x
     */
    @Operation(summary = "创建索引")
    @Parameters(value = {
            @Parameter(name = "indexName", description = "索引名称", required = false)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建索引成功"),
            @ApiResponse(responseCode = "500", description = "创建索引失败")
    })
    @PostMapping("/createIndex")
    public ResponseEntity<Object> createIndex(@RequestParam String indexName) throws IOException {
        CreateIndexResponse index = elasticsearchService.createIndex(indexName);
        return ResponseEntity.ok(index);
    }

    /**
     * 添加文件
     *
     * @param indexName x
     * @param id        x
     * @return x
     * @throws IOException x
     */
    @Operation(summary = "添加文件")
    @Parameters(value = {
            @Parameter(name = "indexName", description = "索引名称", required = false),
            @Parameter(name = "id", description = "文档ID", required = false)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "添加文件成功"),
            @ApiResponse(responseCode = "500", description = "添加文件失败")
    })
    @GetMapping("/addDocument")
    public ResponseEntity<String> addDocument(@RequestParam String indexName, @RequestParam String id) throws IOException {
        log.info("\n添加文档开始" + indexName + " " + id);
        elasticsearchService.addDocument(indexName, id);
        return ResponseEntity.ok("文档添加成功，indexName: " + indexName + "，id： " + id);
    }

    /**
     * 搜索
     *
     * @param indexName x
     * @return x
     * @throws IOException x
     */
    @Operation(summary = "createIndex")
    @Parameters(value = {
            @Parameter(name = "indexName", description = "索引名称", required = false)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "搜索成功"),
            @ApiResponse(responseCode = "500", description = "搜索失败")
    })
    @GetMapping("/createIndex")
    public ResultEntity<CreateIndexResponse> search(@RequestParam String indexName) throws IOException {
        log.info("\ncreateIndex indexName: {}", indexName);
        CreateIndexResponse index = elasticsearchService.createIndex(indexName);
        return ResultEntity.success(index);
    }

    @Operation(summary = "按名称精确搜索")
    @GetMapping("/search/name/{name}")
    public ResultEntity<List<XProducts>> searchByName(@PathVariable String name) {
        log.info("-----------searchByName-");
        List<XProducts> products = elasticsearchService.searchByName(name);
        return ResultEntity.success(products);
    }

    /**
     * 按名称模糊搜索
     */
    @Operation(summary = "按名称模糊搜索")
    @GetMapping("/search/name-containing/{name}")
    public ResultEntity<List<XProducts>> searchByNameContaining(@PathVariable String name) {
        log.info("-----------searchByNameContaining-");
        return ResultEntity.success(elasticsearchService.searchByNameContaining(name));
    }

    /**
     * 添加商品
     */
    @Operation(summary = "添加商品")
    @PostMapping("/add")
    public ResultEntity<XProducts> addProduct(@RequestBody XProducts product) {
        XProducts xProducts = elasticsearchService.saveProduct(product);
        return ResultEntity.success(xProducts);
    }
}
