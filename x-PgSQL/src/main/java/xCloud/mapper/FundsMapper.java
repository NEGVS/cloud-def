package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import xCloud.entity.Account;
import xCloud.entity.Funds;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 17:17
 * @ClassName AccountMapper
 */
public interface FundsMapper extends BaseMapper<Funds>{

    /**
     * 根据用户ID查询账户，并加悲观锁
     * 对应JPA的PESSIMISTIC_WRITE锁
     */
    @Select("SELECT * FROM account WHERE user_id = #{userId} FOR UPDATE")
    Optional<Account> findByUserIdForUpdate(@Param("userId") Long userId);

    @Update("UPDATE funds SET balance = balance - #{amount}, version = version + 1, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND version = #{version} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount, @Param("version") Integer version);


    //（加余额方法）
    @Update("UPDATE funds SET balance = balance + #{amount}, version = version + 1, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND version = #{version}")
    int updateBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount, @Param("version") Integer version);

    @Update("UPDATE funds SET balance = balance + #{amount}, version = version + 1, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND version = #{version}")
    int addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount, @Param("version") Integer version);

    // 新增：插入支付日志
    @Insert("INSERT INTO payment_logs (order_no, user_id, amount,actual_deducted, status, error_message) " +
            "VALUES (#{orderNo}, #{userId}, #{amount}, #{actualDeducted}, #{status}, #{errorMessage})")
    int insertPaymentLog(@Param("orderNo") String orderNo, @Param("userId") Long userId,
                         @Param("amount") BigDecimal amount,@Param("actualDeducted") BigDecimal actualDeducted, @Param("status") String status,
                         @Param("errorMessage") String errorMessage);

    // 新增：更新日志状态
    @Update("UPDATE payment_logs SET status = #{status}, actual_deducted = #{actualDeducted}, error_message = #{errorMessage} " +
            "WHERE order_no = #{orderNo}")
    int updatePaymentLogStatus(@Param("orderNo") String orderNo, @Param("status") String status, @Param("actualDeducted") BigDecimal actualDeducted,
                               @Param("errorMessage") String errorMessage);

    // 新增：从日志获取已扣金额（仅针对FAILED状态，用于补偿）
    @Select("SELECT amount FROM payment_logs WHERE order_no = #{orderNo} AND status = 'FAILED' AND actual_deducted > 0 LIMIT 1")
    BigDecimal getDeductedAmountFromLog(@Param("orderNo") String orderNo);


}