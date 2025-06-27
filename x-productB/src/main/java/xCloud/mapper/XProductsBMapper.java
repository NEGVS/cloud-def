package xCloud.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import xCloud.entity.XProductsB;

/**
* @author andy_mac
* @description 针对表【x_products(商品表)】的数据库操作Mapper
* @createDate 2025-02-21 14:56:12
* @Entity generator.domain.XProducts
*/
//@Mapper
@Repository
public interface XProductsBMapper extends BaseMapper<XProductsB> {

    XProductsB getProductById(Long productId);
}




