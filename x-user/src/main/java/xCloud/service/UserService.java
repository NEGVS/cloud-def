package xCloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.User;
import xCloud.entity.UserDTO;

import java.io.IOException;
import java.util.Map;

/**
 * @author AndyFan
 * @description 针对表【sys_user】的数据库操作Service
 * @createDate 2025-04-07 10:51:53
 */
public interface UserService extends IService<User> {
    /**
     * 1-新增
     *
     * @param dto dto
     * @return 成功条数
     */
    Map<String, Object> add(UserDTO dto);

    /**
     * 2-删除
     *
     * @param dto dto
     * @return 成功条数
     */
    Map<String, Object> delete(UserDTO dto);

    /**
     * 3-更新
     *
     * @param dto dto
     * @return 成功条数
     */
    Map<String, Object> update(UserDTO dto);

    /**
     * 4-查询-列表
     *
     * @param dto 列表搜索
     * @return 列表
     */
    Map<String, Object> list(UserDTO dto);

    /**
     * 4.1-查询-详情
     *
     * @param dto
     * @return 基本信息
     */
    Map<String, Object> detail(UserDTO dto);

//
//    /**
//     * 5-导入
//     *
//     * @param multipartFile 文件流
//     * @param userId        用户id
//     * @param response      响应流
//     */
//    void importFile(MultipartFile multipartFile, String userId, HttpServletResponse response) throws IOException;
//
//    /**
//     * 5.1-下载导入模板
//     */
//    void downloadTemplate(HttpServletResponse response) throws Exception;
//
//    /**
//     * 6-导出
//     *
//     * @param dto 搜索条件
//     */
//    void exportFile(UserDTO dto, HttpServletResponse response) throws Exception;

}
