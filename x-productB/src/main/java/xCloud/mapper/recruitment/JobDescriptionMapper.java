package xCloud.mapper.recruitment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xCloud.entity.recruitment.JobDescription;

import java.util.List;

/**
 * JD Mapper
 * @author Claude
 * @date 2026-08-18
 */
@Mapper
public interface JobDescriptionMapper extends BaseMapper<JobDescription> {

    /**
     * 根据创建人查询JD列表
     */
    List<JobDescription> selectByCreatorId(Long creatorId);

    /**
     * 根据状态查询JD列表
     */
    List<JobDescription> selectByStatus(String status);
}
