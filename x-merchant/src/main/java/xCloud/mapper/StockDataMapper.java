package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import xCloud.entity.StockData;

import java.util.List;

/**
 * @author AndyFan
 * @description 针对表【stock_data】的数据库操作Mapper
 * @createDate 2025-06-10 14:35:31
 */
@Repository
public interface StockDataMapper extends BaseMapper<StockData> {
    /**
     * 1-新增
     *
     * @param entity entity
     * @return int
     */
    int insertStockData(StockData entity);

    /**
     * 2-删除
     *
     * @param entity
     * @return
     */
    int deleteStockData(StockData entity);

    /**
     * 3-修改
     *
     * @param entity entity
     * @return int
     */
    int updateStockData(StockData entity);

    /**
     * 4-查询
     *
     * @param entity entity
     * @return List
     */
    List<StockData> selectStockData(@Param("entity") StockData entity);

    /**
     * 4-查询
     *
     * @param page   page
     * @param entity entity
     * @return Page
     */
    Page<StockData> selectStockData(@Param("page") Page<StockData> page, @Param("entity") StockData entity);
}
