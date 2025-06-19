package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import xCloud.entity.User;

import java.util.List;


/**
 * @author AndyFan
 * @description 针对表【sys_user】的数据库操作Mapper
 * @createDate 2025-04-07 10:51:53
 */
@Repository
public interface UserMapper extends BaseMapper<User> {
    /**
     * 1-新增
     *
     * @param entity entity
     * @return int
     */
    int insertUser(User entity);

    /**
     * 2-删除
     *
     * @param entity
     * @return
     */
    int deleteUser(User entity);

    /**
     * 3-修改
     *
     * @param entity entity
     * @return int
     */
    int updateUser(User entity);

    /**
     * 4-查询
     *
     * @param entity entity
     * @return List
     */
    List<User> selectUser(@Param("entity") User entity);

    /**
     * 4-查询
     *
     * @param page   page
     * @param entity entity
     * @return Page
     */
    Page<User> selectUser(@Param("page") Page<User> page, @Param("entity") User entity);
}
