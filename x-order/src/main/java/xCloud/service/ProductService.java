package xCloud.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xCloud.entity.XProducts;

@FeignClient("shop-product")
public interface ProductService {

    @GetMapping("/product/find/{pid}")
    XProducts findById(@PathVariable("pid") String pid);
}
