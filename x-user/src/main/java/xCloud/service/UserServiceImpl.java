package xCloud.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import xCloud.entity.user.User;
import xCloud.entity.user.UserDTO;
import xCloud.entity.user.UserVO;
import xCloud.mapper.UserMapper;

import java.util.List;
import java.util.Map;

/**
 * @author andy_mac
 * @description 针对表【sys_user】的数据库操作Service实现
 * @createDate 2025-04-07 10:51:53
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 1-新增
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public Map<String, Object> add(UserDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return null;
        }
        //dto-->entity
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        int count = userMapper.insertUser(user);
        return null;
    }

    /**
     * 2-删除
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public Map<String, Object> delete(UserDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return null;

        }
        //dto-->entity
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        int count = userMapper.deleteUser(user);
        return null;

    }

    /**
     * 3-更新
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public Map<String, Object> update(UserDTO dto) {
        if (ObjectUtil.isEmpty(dto) && ObjectUtil.isEmpty(dto.getId())) {
            return null;

        }
        //dto-->entity
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        int count = userMapper.updateUser(user);
        return null;

    }

    /**
     * 4-查询-列表/搜索
     *
     * @param dto 列表搜索
     * @return Map
     */
    @Override
    public Map<String, Object> list(UserDTO dto) {
        if (ObjectUtil.isEmpty(dto)) {
            return null;

        }
        Page<User> page = new Page<>(dto.getCurrent(), dto.getSize());
        //dto-->entity
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        //分页查询
        Page<User> users = userMapper.selectUser(page, user);
        if (ObjectUtil.isNotEmpty(users)) {
            //entity-->vo
            List<UserVO> userVOS = users.getRecords().stream().map(userTemp -> {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(userTemp, userVO);
                return userVO;
            }).toList();
            return null;

        }
        return null;

    }

    /**
     * 4.1-查询-详情
     *
     * @param dto dto
     * @return Map
     */
    @Override
    public Map<String, Object> detail(UserDTO dto) {

        //dto-->entity
        User user = new User();
        List<User> users = userMapper.selectUser(user);
        if (ObjectUtil.isNotEmpty(users)) {
            //entity-->vo
            User userTemp = users.get(0);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userTemp, userVO);
            return null;

        }
        return null;

    }

//    /**
//     * 5-导入
//     *
//     * @param multipartFile 文件流
//     * @param userId        用户id
//     * @param response      响应流
//     * @throws IOException
//     */
//    @Override
//    public void importFile(MultipartFile multipartFile, String userId, HttpServletResponse response) throws IOException {
//
//    }
//
//    /**
//     * 5.1-模版下载
//     *
//     * @param response response
//     * @throws Exception Exception
//     */
//    @Override
//    public void downloadTemplate(HttpServletResponse response) throws Exception {
//
//    }
//
//    /**
//     * 6-导出
//     *
//     * @param dto      搜索条件
//     * @param response response
//     * @throws Exception Exception
//     */
//    @Override
//    public void exportFile(UserDTO dto, HttpServletResponse response) throws Exception {
//
//    }

}
