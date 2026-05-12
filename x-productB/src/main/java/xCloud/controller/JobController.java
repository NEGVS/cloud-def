package xCloud.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import xCloud.entity.Result;
import xCloud.entity.job.Job;
import xCloud.entity.job.JobDTO;
import xCloud.entity.job.JobQueryDTO;
import xCloud.entity.job.JobVO;
import xCloud.service.job.JobService;

/**
 * 岗位管理接口
 */
@Slf4j
@Tag(name = "岗位管理", description = "岗位的增删改查、状态管理、禁用管理接口")
@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    @Qualifier("jobCrudService")
    private JobService jobService;

    // ======================== 查询 ========================

    @Operation(summary = "分页查询岗位列表", description = "支持按名称、类型、状态、分类等条件过滤，返回分页结果")
    @PostMapping("/page")
    public Result<Page<JobVO>> pageJobList(@RequestBody JobQueryDTO query) {
        return jobService.pageJobList(query);
    }

    @Operation(summary = "查询岗位详情", description = "根据岗位ID查询完整信息，含详情、审核、道具等全部字段")
    @GetMapping("/detail/{id}")
    public Result<Job> getJobDetail(
            @Parameter(description = "岗位ID", required = true, example = "1") @PathVariable Integer id) {
        return jobService.getJobDetail(id);
    }

    // ======================== 新增 ========================

    @Operation(summary = "新增岗位", description = "创建一条新岗位，默认状态为停招、待提交审核")
    @PostMapping("/add")
    public Result<Void> addJob(@RequestBody JobDTO dto) {
        return jobService.addJob(dto);
    }

    // ======================== 编辑 ========================

    @Operation(summary = "编辑岗位", description = "更新岗位基本信息，id必传；状态、审核状态等管理字段不受影响")
    @PostMapping("/update")
    public Result<Void> updateJob(@RequestBody JobDTO dto) {
        return jobService.updateJob(dto);
    }

    // ======================== 删除 ========================

    @Operation(summary = "删除岗位（软删除）", description = "软删除，设置 deleted_at，数据不物理删除")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteJob(
            @Parameter(description = "岗位ID", required = true, example = "1") @PathVariable Integer id) {
        return jobService.deleteJob(id);
    }

    // ======================== 状态管理 ========================

    @Operation(summary = "修改岗位状态（启停招）", description = "1=招聘中（同步更新发布时间），4=停招")
    @PostMapping("/status/{id}/{status}")
    public Result<Void> updateStatus(
            @Parameter(description = "岗位ID", required = true, example = "1") @PathVariable Integer id,
            @Parameter(description = "目标状态：1招聘中 4停招", required = true, example = "1") @PathVariable Integer status) {
        return jobService.updateStatus(id, status);
    }

    @Operation(summary = "禁用/启用岗位", description = "isDisable=1禁用，0启用；禁用时需传 reason")
    @PostMapping("/disable/{id}/{isDisable}")
    public Result<Void> updateDisable(
            @Parameter(description = "岗位ID", required = true, example = "1") @PathVariable Integer id,
            @Parameter(description = "0启用 1禁用", required = true, example = "1") @PathVariable Integer isDisable,
            @Parameter(description = "禁用理由，启用时可不传") @RequestParam(required = false) String reason) {
        return jobService.updateDisable(id, isDisable, reason);
    }
}
