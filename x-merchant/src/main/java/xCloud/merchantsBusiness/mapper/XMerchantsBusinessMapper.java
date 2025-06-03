package xCloud.merchantsBusiness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import xCloud.merchantsBusiness.entity.XMerchantsBusiness;

import java.util.List;

/**
 * @author AndyFan
 * @description 针对表【x_merchants_business】的数据库操作Mapper
 * @createDate 2025-06-03 09:25:16
 */
@Repository
public interface XMerchantsBusinessMapper extends BaseMapper<XMerchantsBusiness> {
    /**
     * 1-新增
     *
     * @param entity entity
     * @return int
     */
    int insertXMerchantsBusiness(XMerchantsBusiness entity);

    /**
     * 2-删除
     *
     * @param entity
     * @return
     */
    int deleteXMerchantsBusiness(XMerchantsBusiness entity);

    /**
     * 3-修改
     *
     * @param entity entity
     * @return int
     */
    int updateXMerchantsBusiness(XMerchantsBusiness entity);

    /**
     * 4-查询
     *
     * @param entity entity
     * @return List
     */
    List<XMerchantsBusiness> selectXMerchantsBusiness(@Param("entity") XMerchantsBusiness entity);

    /**
     * 4-查询
     *
     * @param page   page
     * @param entity entity
     * @return Page
     */
    Page<XMerchantsBusiness> selectXMerchantsBusiness(@Param("page") Page<XMerchantsBusiness> page, @Param("entity") XMerchantsBusiness entity);
}
