package xCloud.mapper.job;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import xCloud.entity.job.Job;
import xCloud.entity.job.JobQueryDTO;
import xCloud.entity.job.JobVO;

/**
 * 岗位主表 Mapper
 */
@Repository
public interface JobMapper extends BaseMapper<Job> {

    /**
     * 分页查询岗位列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<JobVO> pageJobList(Page<JobVO> page, @Param("q") JobQueryDTO query);

    /**
     * 查询岗位详情（含全部字段）
     *
     * @param id 岗位ID
     * @return 岗位详情
     */
    Job getJobDetail(@Param("id") Integer id);
}
