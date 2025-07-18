package xcloud.xproduct.config.elasticsearch;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xcloud.xproduct.domain.XProducts;
import xcloud.xproduct.domain.XProductsDocument;
import xcloud.xproduct.mapper.XProductsMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/7/16 16:15
 * @ClassName XProductSyncService
 */
@Service
@Slf4j
public class XProductSyncService {

    @Resource
    ElasticsearchTemplate elasticsearchTemplate;

    @Resource
    XProductsMapper xProductsMapper;

    /**
     * 定时任务同步商品信息到Elasticsearch
     */
    @Scheduled(fixedRate = 1000 * 60)
    public void syncProducts() {
        log.info("\n开始同步商品数据-->ES，find all");
        //1-find all，
        List<XProducts> xProducts = xProductsMapper.selectList(null);


        List<XProductsDocument> documents = xProducts.stream().map(
                p -> {
                    XProductsDocument xProductsDocument = new XProductsDocument();
                    BeanUtils.copyProperties(p, xProductsDocument);
                    return xProductsDocument;
                }
        ).collect(Collectors.toList());
        log.info("\n开始同步商品数据-->ES，save");
        Iterable<XProductsDocument> save = elasticsearchTemplate.save(documents);
        save.forEach(
                p -> {
                    log.info("\n保存成功：{}", p);
                }
        );
    }


}
