package xCloud.merchantsBusiness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.merchantsBusiness.entity.XMerchantsBusiness;
import xCloud.merchantsBusiness.entity.XMerchantsBusinessDTO;

import java.io.IOException;

/**
 * @author AndyFan
 * @description 针对表【x_merchants_business】的数据库操作Service
 * @createDate 2025-06-03 09:25:16
 */
public interface XMerchantsBusinessService extends IService<XMerchantsBusiness> {
    /**
     * 1-新增
     *
     * @param dto dto
     * @return 成功条数
     */
    ResultEntity add(XMerchantsBusinessDTO dto);

    /**
     * 2-删除
     *
     * @param dto dto
     * @return 成功条数
     */
    ResultEntity delete(XMerchantsBusinessDTO dto);

    /**
     * 3-更新
     *
     * @param dto dto
     * @return 成功条数
     */
    ResultEntity update(XMerchantsBusinessDTO dto);

    /**
     * 4-查询-列表
     *
     * @param dto 列表搜索
     * @return 列表
     */
    ResultEntity list(XMerchantsBusinessDTO dto);

    /**
     * 4.1-查询-详情
     *
     * @param dto
     * @return 基本信息
     */
    ResultEntity detail(XMerchantsBusinessDTO dto);


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
    void exportFile(XMerchantsBusinessDTO dto, HttpServletResponse response) throws Exception;

}
