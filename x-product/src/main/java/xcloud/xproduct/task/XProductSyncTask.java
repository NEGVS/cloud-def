package xcloud.xproduct.task;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xcloud.xproduct.mapper.XProductsMapper;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/3/4 16:06
 * @ClassName XProductSyncTask
 */
@Service
public class XProductSyncTask {
    @Resource
    XProductsMapper productsMapper;

    @Resource
    private ElasticsearchRestTemplate esTemplate;

    public void syncProductsToEs() {
        System.out.println("同步商品");
    }
}
