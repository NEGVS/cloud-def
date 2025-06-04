package xCloud.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.entity.merchants.Merchants;
import xCloud.entity.merchants.MerchantsDTO;
import xCloud.entity.merchants.MerchantsVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author AndyFan
 * @description 针对表【x_merchants】的数据库操作Service
 * @createDate 2025-04-09 09:38:50 */
public interface MerchantsService extends IService<Merchants> {
    /**
     * 1-新增
     *
     * @param dto dto
     * @return 成功条数
     */
    ResultEntity<Merchants> add(Merchants dto);

    /**
     * 2-删除
     *
     * @param dto dto
     * @return 成功条数
     */
    Map<String, Object> delete(MerchantsDTO dto);

    /**
     * 3-更新
     *
     * @param dto dto
     * @return 成功条数
     */
    Map<String, Object> update(MerchantsDTO dto);

    /**
     * 4-查询-列表
     *
     * @param dto 列表搜索
     * @return 列表
     */
    ResultEntity<Page<Merchants>> listPage(MerchantsDTO dto);

    /**
     * 4.1-查询-详情
     *
     * @param dto 
     * @return 基本信息
     */
    MerchantsVO detail(MerchantsDTO dto);

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
     * @param dto  搜索条件
     */
    void exportFile(MerchantsDTO dto, HttpServletResponse response) throws Exception;

    /**
     * 4.2-查询-列表-包含商品
     *
     * @param dto 列表搜索
     * @return 列表
     */
    List<Merchants> listMerchantAndProduct(MerchantsDTO dto);
}