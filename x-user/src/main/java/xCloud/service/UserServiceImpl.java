package xCloud.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xCloud.entity.LoginDTO;
import xCloud.entity.LoginVO;
import xCloud.entity.User;
import xCloud.entity.UserDTO;
import xCloud.entity.UserVO;
import xCloud.exception.BusinessException;
import xCloud.mapper.UserMapper;
import xCloud.util.JwtUtil;

import java.util.Date;

/**
 * @author andy_mac
 * @description 针对表【sys_user】的数据库操作Service实现
 * @createDate 2025-04-07 10:51:53
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 账号状态与删除标志常量（对齐 sql.sql 注释）
    private static final String STATUS_DISABLED = "1"; // 帐号停用
    private static final String DEL_FLAG_NORMAL = "0"; // 未删除
    private static final String DEL_FLAG_DELETED = "2"; // 已删除

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private PasswordEncoder passwordEncoder;

    // ==========【登录：DB校验 + BCrypt比对 + 签发JWT】==========
    @Override
    public LoginVO login(LoginDTO dto, String loginIp) {
        User user = userMapper.selectByUserName(dto.getUserName());
        // 用户不存在与密码错误返回同一提示，避免账号探测
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException("账号已停用，请联系管理员");
        }
        // 更新最后登录信息（IP+时间）
        userMapper.updateLoginInfo(user.getUserId(), loginIp);
        // 签发令牌
        String token = jwtUtil.generateToken(user.getUserId(), user.getUserName());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setNickName(user.getNickName());
        vo.setAvatar(user.getAvatar());
        log.info("用户登录成功：userId={}, userName={}", user.getUserId(), user.getUserName());
        return vo;
    }

    // ==========【详情：不返回密码】==========
    @Override
    public UserVO getUser(Long userId) {
        User user = getById(userId);
        if (user == null || DEL_FLAG_DELETED.equals(user.getDelFlag())) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void sendOrderNotification(String orderId, Long userId) {
        log.info("发送订单通知给用户 {}，订单ID: {}", userId, orderId);
    }

    // ==========【1-新增：密码BCrypt加密后落库】==========
    @Override
    public Long add(UserDTO dto) {
        if (ObjectUtil.isEmpty(dto) || StrUtil.isBlank(dto.getUserName())) {
            throw new BusinessException("用户账号不能为空");
        }
        // 账号唯一校验
        if (userMapper.selectByUserName(dto.getUserName()) != null) {
            throw new BusinessException("用户账号已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        // 密码加密，未传密码时给随机默认（此处要求必传）
        if (StrUtil.isBlank(dto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUserId(null); // 自增主键，防止外部传入
        user.setDelFlag(DEL_FLAG_NORMAL);
        user.setCreateTime(new Date());
        save(user);
        return user.getUserId();
    }

    // ==========【2-删除：逻辑删除】==========
    @Override
    public boolean delete(Long userId) {
        if (ObjectUtil.isEmpty(userId)) {
            throw new BusinessException("用户ID不能为空");
        }
        User update = new User();
        update.setUserId(userId);
        update.setDelFlag(DEL_FLAG_DELETED);
        update.setUpdateTime(new Date());
        return updateById(update);
    }

    // ==========【3-更新：不允许通过此接口改密码，密码走独立接口】==========
    @Override
    public boolean update(UserDTO dto) {
        if (ObjectUtil.isEmpty(dto) || ObjectUtil.isEmpty(dto.getUserId())) {
            throw new BusinessException("用户ID不能为空");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(null); // 防止明文密码经此路径覆盖
        user.setUpdateTime(new Date());
        return updateById(user);
    }

    // ==========【4-分页查询：按条件动态过滤，只返回未删除用户】==========
    @Override
    public IPage<UserVO> list(UserDTO dto) {
        Page<User> page = new Page<>(dto.getCurrent() <= 0 ? 1 : dto.getCurrent(), dto.getSize() <= 0 ? 10 : dto.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDelFlag, DEL_FLAG_NORMAL)
                .like(StrUtil.isNotBlank(dto.getUserName()), User::getUserName, dto.getUserName())
                .like(StrUtil.isNotBlank(dto.getNickName()), User::getNickName, dto.getNickName())
                .eq(StrUtil.isNotBlank(dto.getPhonenumber()), User::getPhonenumber, dto.getPhonenumber())
                .eq(StrUtil.isNotBlank(dto.getStatus()), User::getStatus, dto.getStatus())
                .eq(ObjectUtil.isNotEmpty(dto.getDeptId()), User::getDeptId, dto.getDeptId())
                .orderByDesc(User::getCreateTime);
        Page<User> userPage = page(page, wrapper);
        // entity page -> vo page，剔除密码
        return userPage.convert(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        });
    }
}
