package xcloud.xproduct.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xcloud.xproduct.domain.XProducts;
import xcloud.xproduct.mapper.XProductsMapper;
import xcloud.xproduct.service.XProductsService;

/**
 * @author andy_mac
 * @description 针对表【x_products(商品表)】的数据库操作Service实现
 * @createDate 2025-02-21 14:56:12
 */
@Service
public class XProductsServiceImpl extends ServiceImpl<XProductsMapper, XProducts>
        implements XProductsService {

    @Resource
    XProductsMapper productsMapper;

    @Override
    public XProducts getProductById(Long productId) {

        XProducts xProducts = productsMapper.selectById(productId);

        System.out.println(JSONUtil.toJsonStr(xProducts));

        XProducts productById = productsMapper.getProductById(productId);
        System.out.println(JSONUtil.toJsonStr(productById));

        return productsMapper.selectById(productId);
    }
}




