package xCloud.service;


import com.baomidou.mybatisplus.extension.service.IService;
import xCloud.domain.XProductsB;

/**
* @author andy_mac
* @description 针对表【x_products(商品表)】的数据库操作Service
* @createDate 2025-02-21 14:56:12
*/
public interface XProductsBService extends IService<XProductsB> {

    XProductsB getProductById(Long productId);
}
