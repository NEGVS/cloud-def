package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import xCloud.entity.Logs;


public interface LogsMapper extends BaseMapper<Logs> {
    // 插入日志
    @Insert("INSERT INTO logs (remark, note) " +
            "VALUES (#{remark},  #{note})")
    int insertLogs(@Param("remark") String remark,
                         @Param("note") String note);
}
