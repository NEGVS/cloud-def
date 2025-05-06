package xCloud.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/4/21 14:47
 * @ClassName BookingTools
 */
@Component
public class BookingTools {
//    @Autowired
//    XProductsService productsService;

    @Tool
    public String getProducts(Integer product_id) {
//        Products products = productsService.getById(product_id);
        return "products";
    }

    @Tool
    public boolean deleteProducts(Integer product_id) {
        boolean b = false;
        return b;
    }

}
