package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import xCloud.entity.Stock;

import java.util.List;

/**
 * @author AndyFan
 * @description 针对表【stock】的数据库操作Mapper
 * @createDate 2025-06-10 13:59:52
 */
@Repository
public interface StockMapper extends BaseMapper<Stock> {
    /**
     * 1-新增
     *
     * @param entity entity
     * @return int
     */
    int insertStock(Stock entity);

    int insertStockList(List<Stock> entityList);

    /**
     * 2-删除
     *
     * @param entity
     * @return
     */
    int deleteStock(Stock entity);

    /**
     * 3-修改
     *
     * @param entity entity
     * @return int
     */
    int updateStock(Stock entity);

    /**
     * 4-查询
     *
     * @param entity entity
     * @return List
     */
    List<Stock> selectStock(@Param("entity") Stock entity);

    /**
     * 4-查询
     *
     * @param page   page
     * @param entity entity
     * @return Page
     */
    Page<Stock> selectStock(@Param("page") Page<Stock> page, @Param("entity") Stock entity);
}
