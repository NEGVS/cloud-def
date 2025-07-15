package xcloud.xproduct.config.elasticsearch;

//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.core.IndexResponse;

import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xcloud.xproduct.domain.XProducts;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 索引数据
 * @Author Andy Fan
 * @Date 2025/3/6 16:13
 * @ClassName ElasticsearchService
 * 3. 创建 Elasticsearch 操作 Service
 * 创建索引、添加文档、搜索数据。
 */
@Slf4j
@Service
public class ElasticsearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private XProductRepository xProductRepository;

    @PostConstruct // 启动时执行
    public void initData() {
        XProducts product = new XProducts();
        product.setProduct_id(Long.valueOf(11111111));
        product.setName("Laptop");
        product.setDescription("High-performance laptop");
        log.info("\n\n-----------启动时执行--ElasticsearchService--initData" + JSONUtil.toJsonStr(product));
        try {
            XProducts save = xProductRepository.save(product);
            log.info("\n\n-----------启动时执行--ElasticsearchService--initData--保存成功" + JSONUtil.toJsonStr(save));
        } catch (Exception e) {
            log.error("\n\n-----------启动时执行异常--ElasticsearchService--initData" + e.getMessage());
        }
    }
    /**
     * 保存商品信息
     *
     * @param product x
     * @return x
     */
    public XProducts saveProduct(XProducts product) {
        return xProductRepository.save(product);
    }

    /**
     * 根据商品名称搜索商品信息
     *
     * @param name x
     * @return x
     */
    public List<XProducts> searchByName(String name) {
        log.info("--------service---searchByName-");
        return xProductRepository.findByName(name);
    }

    /**
     * 根据名称模糊搜索
     * @param name  x
     * @return  x
     */
    public List<XProducts> searchByNameContaining(String name) {
        log.info("--------service---searchByNameContaining-");
        return xProductRepository.findByNameContaining(name);
    }

    /**
     * 创建索引
     *
     * @param indexName x
     * @throws IOException x
     */
    public void createIndex(String indexName) throws IOException {
        try {
            elasticsearchClient.indices().create(c -> c.index(indexName));
            System.out.println("创建索引成功" + indexName);
        } catch (Exception e) {
            log.error("\n创建索引失败", e);
        }
    }

    /**
     * 添加文档
     *
     * @param indexName x
     * @param id        x
     * @throws IOException x
     */
    public void addDocument(String indexName, String id) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "测试name");
        data.put("description", "测试数据");
        data.put("price", 100);
        data.put("city", "shanghai");
        IndexResponse response = elasticsearchClient.index(i -> i.index(indexName).id(id).document(data));
        System.out.println("添加文档成功,result:" + response.result() + ", id: " + response.id());
    }


}
