package xCloud.feignClient.productClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 使用Fegin实现微服务调用---Feign 客户端调用商品服务
 */

//声明调用的提供者的name
//@FeignClient("x-product")
@FeignClient(name = "x-product", url = "${nacos.discovery.server-addr:localhost:8848}")
public interface ProductClient {

    //指定调用提供者的哪个方法
    //@FeignClient+@GetMapping 就是一个完整的请求路径 http://service-product/find/{pid}

    @GetMapping("/product/find/{pid}")
    Products findById(@PathVariable("pid") String pid);

    @GetMapping("/products/{id}")
    Products getProduct(@PathVariable("id") Long productId);

    @PostMapping("/products/reserve")
    boolean reserveStock(@RequestParam("productId") Long productId, @RequestParam("quantity") int quantity);
}
