package xcloud.xproduct.task;

import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xcloud.xproduct.domain.XProducts;
import xcloud.xproduct.mapper.XProductsMapper;

import java.util.List;

/**
 * @Description 数据同步 定时同步（MySQL -> ES）
 * @Author Andy Fan
 * @Date 2025/3/4 16:06
 * @ClassName XProductSyncTask
 */
@Service
public class XProductSyncTask {
    @Resource
    XProductsMapper productsMapper;


//    @Scheduled(fixedRate = 1000 * 60 * 60)
    public void syncProductsToEs() {
        List<XProducts> products = productsMapper.selectList(null);
        System.out.println("同步商品");
    }
}
