package xcloud.xproduct.config.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/3/6 17:24
 * @ClassName ElasticsearchTestController
 */
@RestController
@Slf4j
public class ElasticsearchTestController {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ElasticsearchQueryService elasticsearchQueryService;

    @GetMapping("/create-index")
    public String createIndex(@RequestParam String indexName) throws IOException {
        elasticsearchService.createIndex(indexName);
        return "创建索引成功,indexName: " + indexName;
    }

    @GetMapping("/add-document")
    public String addDocument(@RequestParam String indexName, @RequestParam String id) throws IOException {
        elasticsearchService.addDocument(indexName, id);
        return "文档添加成功，indexName: " + indexName + "，id： " + id;
    }

    @GetMapping("/search")
    public String createInde2x(@RequestParam String indexName) throws IOException {
        elasticsearchService.createIndex(indexName);
        return "创建索引成功" + indexName;
    }

}
