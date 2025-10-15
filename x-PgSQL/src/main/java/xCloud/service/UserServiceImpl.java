package xCloud.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xCloud.entity.User;
import xCloud.mapper.UserMapper;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 16:05
 * @ClassName UserServiceImpl
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    // 内置方法：save()、getById()、updateById()、removeById()
}