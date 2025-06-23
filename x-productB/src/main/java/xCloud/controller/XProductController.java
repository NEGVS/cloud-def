package xCloud.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.domain.XProductsB;
import xCloud.service.XProductsBService;

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
    XProductsBService xProductsService;

    @GetMapping("/find/{id}")

    public XProductsB find(@PathVariable("id") String id) {


        XProductsB productById = xProductsService.getProductById(Long.valueOf(id));

        return productById;
    }
}
