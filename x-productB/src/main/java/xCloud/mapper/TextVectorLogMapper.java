package xCloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xCloud.entity.TextVectorLog;

import java.util.List;

@Mapper
public interface TextVectorLogMapper extends BaseMapper<TextVectorLog> {

    /**
     * 批量插入向量日志（单条 SQL，性能优于循环 insert）
     */
    @Insert("<script>" +
            "INSERT INTO text_vector_log (id, text, vector, create_time, source, remark) VALUES " +
            "<foreach collection='list' item='e' separator=','>" +
            "(#{e.id}, #{e.text}, #{e.vector}, #{e.create_time}, #{e.source}, #{e.remark})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<TextVectorLog> list);
}
