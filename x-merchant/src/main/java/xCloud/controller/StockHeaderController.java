package xCloud.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.entity.StockHeader;
import xCloud.entity.StockHeaderDTO;
import xCloud.entity.StockHeaderVO;
import xCloud.service.StockHeaderService;

/**
 * @Description StockHeader接口
 * @Author Andy Fan
 * @Date 2025-06-10 14:25:33
 * @ClassName StockHeaderController
 */
@RestController
@RequestMapping("biz/stockHeader")
@Slf4j
@Tag(name = "StockHeader接口", description = "StockHeader接口")
public class StockHeaderController {

	@Autowired
	private StockHeaderService stockHeaderService;

    /**
     * 1-增加
     *
     * @param dto 前端请求dto
     * @return 查询结果
     */
    @Operation(summary = "新增")
    @ApiResponse(responseCode = "200", description = "新增成功", content = @Content(schema = @Schema(implementation = StockHeader.class)))
    @PostMapping("/add")
    public ResultEntity<StockHeader> add(@RequestBody StockHeaderDTO dto) {
        log.info("新增数据参数：{}", JSONUtil.toJsonStr(dto));
        return stockHeaderService.add(dto);
    }

    /**
     * 2-删除
     *
     * @param dto 前端请求dto
     * @return 查询结果
     */
    @Operation(summary = "删除", description = "删除")
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = StockHeader.class)))
    @PostMapping("/delete")
    public ResultEntity<StockHeader> delete(@RequestBody StockHeaderDTO dto) {
        log.info("更新数据参数：{}", JSONUtil.toJsonStr(dto));
        return stockHeaderService.delete(dto);
    }

    /**
     * 3-修改
     *
     * @param dto 前端请求VO
     * @return 查询结果
     */
    @Operation(summary = "更新接口", description = "更新数据")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = StockHeader.class)))
    @PostMapping("/update")
    public ResultEntity<StockHeader> update(@RequestBody StockHeaderDTO dto) {
        log.info("更新数据参数：{}", JSONUtil.toJsonStr(dto));
        return stockHeaderService.update(dto);
    }

    /**
     * 4-查询-列表
     *
     * @param dto 列表搜索
     * @return 列表
     */
    @Operation(summary = "获取列表")
    @ApiResponse(responseCode = "200", description = "获取成功", content = @Content(schema = @Schema(implementation = StockHeader.class)))
    @PostMapping(value = "/list")
    public ResultEntity<Page<StockHeader>> list(@RequestBody StockHeaderDTO dto) {
        log.info("列表查询参数：{}", JSONUtil.toJsonStr(dto));
        return stockHeaderService.list(dto);
    }


    /**
     * 4.1-查询-详情
     *
     * @param dto 
     * @return 基本
     */
    @Parameter(name = "stockHeaderId", description = "Id", required = true)
    @Operation(summary = "列表-点击详情", description = "详情数据")
    @ApiResponse(responseCode = "200", description = "详情", content = @Content(schema = @Schema(implementation = StockHeader.class)))
    @PostMapping("/detail")
    public ResultEntity<StockHeaderVO> detail(@RequestBody StockHeaderDTO dto) throws Exception {
        log.info("查询详情参数：{}", JSONUtil.toJsonStr(dto));
        return stockHeaderService.detail(dto);
    }

    /**
     * 5-导入
     *
     * @param multipartFile 文件流
     * @param response      响应流
     */
    @Operation(summary = "导入")
    @Parameter(name = "uploadFile", description = "导入文件", required = true)
    @ApiResponse(responseCode = "200", description = "导入成功")
    @PostMapping("importFile")
    public void importFile(@RequestParam("uploadFile") MultipartFile multipartFile, HttpServletResponse response) throws Exception {
        stockHeaderService.importFile(multipartFile, "userId", response);
    }

    /**
     * 5.1-下载导入模板
     */
    @Operation(summary = "模版下载接口", description = "模版下载接口")
    @ApiResponse(responseCode = "200", description = "模版下载接口")
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        stockHeaderService.downloadTemplate(response);
    }

    /**
     * 6-导出
     */
    @Operation(summary = "导出", description = "导出")
    @PostMapping("/export")
    public void export(@RequestBody StockHeaderDTO dto, HttpServletResponse response) throws Exception {
        log.info("导出的查询参数：{}", JSONUtil.toJsonStr(dto));
        stockHeaderService.exportFile(dto, response);
    }}
