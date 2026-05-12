package xCloud.service.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xCloud.entity.Result;
import xCloud.entity.job.Job;
import xCloud.entity.job.JobDTO;
import xCloud.entity.job.JobQueryDTO;
import xCloud.entity.job.JobVO;

/**
 * 岗位主表 Service
 */
public interface JobService extends IService<Job> {

    /**
     * 分页查询岗位列表
     */
    Result<Page<JobVO>> pageJobList(JobQueryDTO query);

    /**
     * 查询岗位详情
     */
    Result<Job> getJobDetail(Integer id);

    /**
     * 新增岗位
     */
    Result<Void> addJob(JobDTO dto);

    /**
     * 编辑岗位
     */
    Result<Void> updateJob(JobDTO dto);

    /**
     * 删除岗位（软删除）
     */
    Result<Void> deleteJob(Integer id);

    /**
     * 修改岗位状态（启停招）
     *
     * @param id     岗位ID
     * @param status 1招聘中 4停招
     */
    Result<Void> updateStatus(Integer id, Integer status);

    /**
     * 禁用/启用岗位
     *
     * @param id        岗位ID
     * @param isDisable 0启用 1禁用
     * @param reason    禁用理由（启用时可传null）
     */
    Result<Void> updateDisable(Integer id, Integer isDisable, String reason);
}
