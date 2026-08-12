package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xCloud.entity.User;

/**
 * @author AndyFan
 * @description 针对表【sys_user】的数据库操作Mapper
 * @createDate 2025-04-07 10:51:53
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户账号查询用户（登录用）：仅取未删除账号，含密码字段用于校验
     *
     * @param userName 用户账号
     * @return 用户实体，不存在返回null
     */
    User selectByUserName(@Param("userName") String userName);

    /**
     * 更新最后登录信息（IP + 时间），登录成功后异步/同步调用
     *
     * @param userId  用户ID
     * @param loginIp 登录IP
     * @return 影响行数
     */
    int updateLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);
}
