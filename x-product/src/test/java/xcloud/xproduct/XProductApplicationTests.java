package xcloud.xproduct;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import xcloud.xproduct.domain.XProducts;
import xcloud.xproduct.service.XProductsService;

import java.util.List;

@SpringBootTest
class XProductApplicationTests {

    @Resource
    XProductsService xProductsService;
    @Test
    void contextLoads() {
        List<XProducts> list = xProductsService.list();
        System.out.println(JSONUtil.toJsonStr(list));
        System.out.println("-----done");
    }

}
