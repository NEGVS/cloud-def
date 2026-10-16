package xCloud.mapper.recruitment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xCloud.dto.recruitment.ResumeVO;
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
     * 分页查询简历列表（不返回大字段 raw_content）
     *
     * @param page        分页参数
     * @param parseStatus 解析状态过滤（可为空）
     * @return 分页结果
     */
    IPage<ResumeVO> pageResumeList(Page<ResumeVO> page, @Param("parseStatus") String parseStatus);

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
