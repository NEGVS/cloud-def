package xCloud.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xCloud.domain.Products;

/**
 * 使用Fegin实现微服务调用
 */

//声明调用的提供者的name
@FeignClient("shop-product")
public interface ProductService {

    //指定调用提供者的哪个方法
    //@FeignClient+@GetMapping 就是一个完整的请求路径 http://service-product/find/{pid}

    @GetMapping("/product/find/{pid}")
    Products findById(@PathVariable("pid") String pid);
}
