package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xCloud.entity.Order;

import java.util.Optional;

public interface OrderMapper extends BaseMapper<Order> {

    Optional<Order> findByOrderNo(String orderNo);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);
}
