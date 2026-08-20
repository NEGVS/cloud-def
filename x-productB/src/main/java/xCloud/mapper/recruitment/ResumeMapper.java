package xCloud.mapper.recruitment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xCloud.entity.recruitment.Resume;

import java.util.List;

/**
 * 简历 Mapper
 * @author Claude
 * @date 2026-08-18
 */
@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {

    /**
     * 根据解析状态查询简历列表
     */
    List<Resume> selectByParseStatus(@Param("parseStatus") String parseStatus);

    /**
     * 根据向量化状态查询简历列表
     */
    List<Resume> selectByVectorizeStatus(@Param("vectorizeStatus") String vectorizeStatus);

    /**
     * 查询待索引的简历
     */
    List<Resume> selectPendingIndex();
}
