package xcloud.xproduct.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xcloud.xproduct.domain.XProducts;
import xcloud.xproduct.service.XProductsService;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/4/21 14:47
 * @ClassName BookingTools
 */
@Component
public class BookingTools {
    @Autowired
    XProductsService productsService;

    @Tool
    public XProducts getProducts(Integer product_id) {
        XProducts products = productsService.getById(product_id);
        return products;
    }

    @Tool
    public boolean deleteProducts(Integer product_id) {
        boolean b = productsService.removeById(product_id);
        return b;
    }

}
