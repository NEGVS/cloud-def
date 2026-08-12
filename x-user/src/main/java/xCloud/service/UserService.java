package xCloud.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import xCloud.entity.LoginDTO;
import xCloud.entity.LoginVO;
import xCloud.entity.User;
import xCloud.entity.UserDTO;
import xCloud.entity.UserVO;

/**
 * @author AndyFan
 * @description 针对表【sys_user】的数据库操作Service
 * @createDate 2025-04-07 10:51:53
 */
public interface UserService extends IService<User> {

    /**
     * 登录：校验账号密码，成功返回JWT令牌与基础信息
     *
     * @param dto     登录参数
     * @param loginIp 登录IP
     * @return 登录结果
     */
    LoginVO login(LoginDTO dto, String loginIp);

    /**
     * 获取用户详情（不含密码）
     *
     * @param userId 用户ID
     * @return 用户返回对象
     */
    UserVO getUser(Long userId);

    /**
     * RabbitMQ 订单通知
     */
    void sendOrderNotification(String orderId, Long userId);

    /**
     * 1-新增用户
     *
     * @param dto 用户参数
     * @return 新增后的用户ID
     */
    Long add(UserDTO dto);

    /**
     * 2-删除用户（逻辑删除 del_flag=2）
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean delete(Long userId);

    /**
     * 3-更新用户
     *
     * @param dto 用户参数
     * @return 是否成功
     */
    boolean update(UserDTO dto);

    /**
     * 4-分页查询用户列表
     *
     * @param dto 查询与分页参数
     * @return 分页结果
     */
    IPage<UserVO> list(UserDTO dto);
}
