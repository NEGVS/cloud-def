package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import xCloud.entity.StockHeader;

import java.util.List;

/**
 * @author AndyFan
 * @description 针对表【stock_header】的数据库操作Mapper
 * @createDate 2025-06-10 14:25:33
 */
@Repository
public interface StockHeaderMapper extends BaseMapper<StockHeader> {
    /**
     * 1-新增
     *
     * @param entity entity
     * @return int
     */
    int insertStockHeader(StockHeader entity);

    int insertStockHeaderList(List<StockHeader> entityList);

    /**
     * 2-删除
     *
     * @param entity
     * @return
     */
    int deleteStockHeader(StockHeader entity);

    /**
     * 3-修改
     *
     * @param entity entity
     * @return int
     */
    int updateStockHeader(StockHeader entity);

    /**
     * 4-查询
     *
     * @param entity entity
     * @return List
     */
    List<StockHeader> selectStockHeader(@Param("entity") StockHeader entity);

    /**
     * 4-查询
     *
     * @param page   page
     * @param entity entity
     * @return Page
     */
    Page<StockHeader> selectStockHeader(@Param("page") Page<StockHeader> page, @Param("entity") StockHeader entity);
}
