package xCloud.service.job.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import xCloud.entity.Result;
import xCloud.entity.job.Job;
import xCloud.entity.job.JobDTO;
import xCloud.entity.job.JobQueryDTO;
import xCloud.entity.job.JobVO;
import xCloud.mapper.job.JobMapper;
import xCloud.service.job.JobService;

import java.time.LocalDateTime;

/**
 * 岗位主表 ServiceImpl
 */
@Slf4j
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    @Resource
    private JobMapper jobMapper;

    @Override
    public Result<Page<JobVO>> pageJobList(JobQueryDTO query) {
        Page<JobVO> page = new Page<>(query.getCurrentPage(), query.getPageSize());
        Page<JobVO> result = (Page<JobVO>) jobMapper.pageJobList(page, query);
        return Result.success(result);
    }

    @Override
    public Result<Job> getJobDetail(Integer id) {
        Job job = jobMapper.getJobDetail(id);
        if (job == null) {
            return Result.error("岗位不存在");
        }
        return Result.success(job);
    }

    @Override
    public Result<Void> addJob(JobDTO dto) {
        Job job = new Job();
        BeanUtils.copyProperties(dto, job);
        job.setStatus(4);          // 默认停招，发布后才变招聘中
        job.setIsDisable(0);
        job.setAuditType(100);     // 默认待提交审核
        job.setSource(3);          // app创建
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        boolean saved = save(job);
        return saved ? Result.success(null) : Result.error("新增失败");
    }

    @Override
    public Result<Void> updateJob(JobDTO dto) {
        if (dto.getId() == null) {
            return Result.error("id不能为空");
        }
        Job exist = getById(dto.getId());
        if (exist == null || exist.getDeletedAt() != null) {
            return Result.error("岗位不存在");
        }
        BeanUtils.copyProperties(dto, exist, "id", "status", "isDisable", "auditType",
                "createdAt", "source", "createdAdminId");
        exist.setUpdatedAt(LocalDateTime.now());
        boolean updated = updateById(exist);
        return updated ? Result.success(null) : Result.error("更新失败");
    }

    @Override
    public Result<Void> deleteJob(Integer id) {
        Job job = getById(id);
        if (job == null || job.getDeletedAt() != null) {
            return Result.error("岗位不存在");
        }
        job.setDeletedAt(LocalDateTime.now());
        boolean updated = updateById(job);
        return updated ? Result.success(null) : Result.error("删除失败");
    }

    @Override
    public Result<Void> updateStatus(Integer id, Integer status) {
        if (status == null || (status != 1 && status != 4)) {
            return Result.error("状态值非法，1=招聘中，4=停招");
        }
        Job job = getById(id);
        if (job == null || job.getDeletedAt() != null) {
            return Result.error("岗位不存在");
        }
        job.setStatus(status);
        if (status == 1) {
            job.setReleaseTime(LocalDateTime.now());
        }
        job.setUpdatedAt(LocalDateTime.now());
        boolean updated = updateById(job);
        return updated ? Result.success(null) : Result.error("状态更新失败");
    }

    @Override
    public Result<Void> updateDisable(Integer id, Integer isDisable, String reason) {
        if (isDisable == null || (isDisable != 0 && isDisable != 1)) {
            return Result.error("isDisable值非法，0=启用，1=禁用");
        }
        Job job = getById(id);
        if (job == null || job.getDeletedAt() != null) {
            return Result.error("岗位不存在");
        }
        job.setIsDisable(isDisable);
        if (isDisable == 1) {
            job.setDisableReason(reason);
        } else {
            job.setDisableReason(null);
        }
        job.setUpdatedAt(LocalDateTime.now());
        boolean updated = updateById(job);
        return updated ? Result.success(null) : Result.error("操作失败");
    }
}
