package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xCloud.entity.User;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 16:04
 * @ClassName UserMapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 自定义方法可选，如：User selectByName(String name);
}
