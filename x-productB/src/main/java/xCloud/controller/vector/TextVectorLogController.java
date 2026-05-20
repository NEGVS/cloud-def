package xCloud.controller.vector;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xCloud.entity.PageRequest;
import xCloud.entity.Result;
import xCloud.entity.TextVectorLog;
import xCloud.service.vector.TextVectorLogService;

/**
 * @Description 文本向量日志管理
 * @Author Andy Fan
 * @Date 2026/5/20 15:49
 * @ClassName TextVectorLogController
 */
@Slf4j
@Tag(name = "文本向量日志", description = "文本向量日志的增删改查接口")
@RestController
@RequestMapping("text/vector/log")
public class TextVectorLogController {

    @Resource
    private TextVectorLogService textVectorLogService;

    @Operation(summary = "分页查询向量日志", description = "支持按 text 模糊搜索，分页返回向量日志列表")
    @PostMapping("/list")
    public Result<Page<TextVectorLog>> vectorList(@RequestBody PageRequest<TextVectorLog> request) {
        log.info("分页查询向量日志, page: {}, size: {}, data: {}",
                request.getCurrentPage(), request.getPageSize(), request.getData());

        Page<TextVectorLog> page = new Page<>(
                request.getCurrentPage() != null ? request.getCurrentPage() : 1,
                request.getPageSize() != null ? request.getPageSize() : 10
        );

        LambdaQueryWrapper<TextVectorLog> queryWrapper = new LambdaQueryWrapper<>();
        // 防止 data 为 null 时 NPE
        if (request.getData() != null && ObjectUtil.isNotEmpty(request.getData().getText())) {
            queryWrapper.like(TextVectorLog::getText, request.getData().getText());
        }
        // 按创建时间倒序
        queryWrapper.orderByDesc(TextVectorLog::getCreate_time);

        Page<TextVectorLog> resultPage = textVectorLogService.page(page, queryWrapper);
        // vector 字段过长，列表展示截断为前20字符 + "..."
        resultPage.getRecords().forEach(item -> {
            if (item.getVector() != null && item.getVector().length() > 20) {
                item.setVector(item.getVector().substring(0, 20) + "...");
            }
        });
        return Result.success(resultPage);
    }

    @Operation(summary = "新增向量日志", description = "新增一条文本向量日志记录")
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody TextVectorLog textVectorLog) {
        log.info("新增向量日志: {}", textVectorLog);
        return Result.success(textVectorLogService.save(textVectorLog));
    }

    @Operation(summary = "更新向量日志", description = "根据 id 更新文本向量日志记录")
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody TextVectorLog textVectorLog) {
        log.info("更新向量日志, id: {}", textVectorLog.getId());
        return Result.success(textVectorLogService.updateById(textVectorLog));
    }

    @Operation(summary = "删除向量日志", description = "根据 id 删除文本向量日志记录")
    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        log.info("删除向量日志, id: {}", id);
        return Result.success(textVectorLogService.removeById(id));
    }
}
