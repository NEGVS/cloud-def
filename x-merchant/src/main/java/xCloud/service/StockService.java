package xCloud.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.entity.Stock;
import xCloud.entity.StockDTO;
import xCloud.entity.StockVO;

import java.io.IOException;

/**
 * @author AndyFan
 * @description 针对表【stock】的数据库操作Service
 * @createDate 2025-06-10 13:59:52
 */
public interface StockService extends IService<Stock> {
    /**
     * 1-新增
     *
     * @param dto dto
     * @return 成功条数
     */
    ResultEntity<Stock> add(StockDTO dto);

    /**
     * 更新stock
     *
     * @param dateStr dateStr
     */
    public ResultEntity<Stock> test(String dateStr);

    public void test2();

    /**
     * 2-删除
     *
     * @param dto dto
     * @return 成功条数
     */
    ResultEntity<Stock> delete(StockDTO dto);

    /**
     * 3-更新
     *
     * @param dto dto
     * @return 成功条数
     */
    ResultEntity<Stock> update(StockDTO dto);

    /**
     * 4-查询-列表
     *
     * @param dto 列表搜索
     * @return 列表
     */
    ResultEntity<Page<Stock>> list(StockDTO dto);

    /**
     * 4.1-查询-详情
     *
     * @param dto
     * @return 基本信息
     */
    ResultEntity<StockVO> detail(StockDTO dto);


    /**
     * 5-导入
     *
     * @param multipartFile 文件流
     * @param userId        用户id
     * @param response      响应流
     */
    void importFile(MultipartFile multipartFile, String userId, HttpServletResponse response) throws IOException;

    /**
     * 5.1-下载导入模板
     */
    void downloadTemplate(HttpServletResponse response) throws Exception;

    /**
     * 6-导出
     *
     * @param dto 搜索条件
     */
    void exportFile(StockDTO dto, HttpServletResponse response) throws Exception;

}
