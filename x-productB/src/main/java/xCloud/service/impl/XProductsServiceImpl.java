package xCloud.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xCloud.entity.Result;
import xCloud.entity.XProductsB;
import xCloud.mapper.XProductsBMapper;
import xCloud.service.XProductsBService;

/**
 * @author andy_mac
 * @description 针对表【x_products(商品表)】的数据库操作Service实现
 * @createDate 2025-02-21 14:56:12
 */
@Service
public class XProductsServiceImpl extends ServiceImpl<XProductsBMapper, XProductsB>
        implements XProductsBService {

    @Resource
    XProductsBMapper productsMapper;

    @Override
    public XProductsB getProductById(Long productId) {

        XProductsB xProducts = productsMapper.selectById(productId);

        System.out.println(JSONUtil.toJsonStr(xProducts));

        XProductsB productById = productsMapper.getProductById(productId);
        System.out.println(JSONUtil.toJsonStr(productById));

        return productsMapper.selectById(productId);
    }

    @Override
    public Result<XProductsB> addProduct(XProductsB request) {
        int insert = productsMapper.insert(request);
        if (insert > 0) {
            return Result.success(request);
        } else {
            return Result.error("添加失败");
        }
    }
}




