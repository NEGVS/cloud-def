package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xCloud.entity.Merchants;

import java.util.List;

/**
 * @author AndyFan
 * @description 针对表【x_merchants】的数据库操作Mapper
 * @createDate 2025-04-09 09:38:50
 */
@Mapper
public interface MerchantsMapper extends BaseMapper<Merchants> {
    /**
     * 1-新增
     *
     * @param entity entity
     * @return int
     */
    int insertMerchants(Merchants entity);

    /**
     * 2-删除
     *
     * @param entity
     * @return
     */
    int deleteMerchants(Merchants entity);

    /**
     * 3-修改
     *
     * @param entity entity
     * @return int
     */
    int updateMerchants(Merchants entity);

    /**
     * 4-查询
     *
     * @param entity entity
     * @return List
     */
    List<Merchants> selectMerchants(@Param("entity") Merchants entity);

    /**
     * 4-查询
     *
     * @param page   page
     * @param entity entity
     * @return Page
     */
    Page<Merchants> selectMerchants(@Param("page") Page<Merchants> page, @Param("entity") Merchants entity);
}
