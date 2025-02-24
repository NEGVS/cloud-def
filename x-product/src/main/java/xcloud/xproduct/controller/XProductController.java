package xcloud.xproduct.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xcloud.xproduct.domain.XProducts;
import xcloud.xproduct.service.XProductsService;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/2/21 14:11
 * @ClassName ProductController
 */
@RestController
@Slf4j
@RequestMapping("/product")
public class XProductController {

    @Resource
    XProductsService xProductsService;

    @GetMapping("/find/{id}")

    public XProducts find(@PathVariable("id") String id) {

        XProducts productById = xProductsService.getProductById(Long.valueOf(id));

        return productById;
    }
}
