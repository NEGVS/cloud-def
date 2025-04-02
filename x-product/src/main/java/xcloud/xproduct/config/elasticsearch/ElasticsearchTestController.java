package xcloud.xproduct.config.elasticsearch;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
     * 创建索引
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
    @GetMapping("/create-index")
    public ResponseEntity<Object> createIndex(@RequestParam String indexName) throws IOException {
        elasticsearchService.createIndex(indexName);
        return ResponseEntity.ok("创建索引成功,indexName: " + indexName);
    }

    /**
     * 添加文件
     *
     * @param indexName x
     * @param id x
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
    @GetMapping("/add-document")
    public String addDocument(@RequestParam String indexName, @RequestParam String id) throws IOException {
        elasticsearchService.addDocument(indexName, id);
        return "文档添加成功，indexName: " + indexName + "，id： " + id;
    }

    /**
     * 搜索
     *
     * @param indexName x
     * @return x
     * @throws IOException x
     */
    @Operation(summary = "搜索")
    @Parameters(value = {
            @Parameter(name = "indexName", description = "索引名称", required = false)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "搜索成功"),
            @ApiResponse(responseCode = "500", description = "搜索失败")
    })
    @GetMapping("/search")
    public String createInde2x(@RequestParam String indexName) throws IOException {
        elasticsearchService.createIndex(indexName);
        return "创建索引成功" + indexName;
    }

}
