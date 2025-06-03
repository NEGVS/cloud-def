package xCloud.merchantsBusiness.controller;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xCloud.entity.ResultEntity;
import xCloud.merchantsBusiness.entity.XMerchantsBusiness;
import xCloud.merchantsBusiness.entity.XMerchantsBusinessDTO;
import xCloud.merchantsBusiness.service.XMerchantsBusinessService;


/**
 * @Description XMerchantsBusiness接口
 * @Author Andy Fan
 * @Date 2025-06-03 09:25:16
 * @ClassName XMerchantsBusinessController
 */
@RestController
@RequestMapping("biz/xMerchantsBusiness")
@Slf4j
@Tag(name = "XMerchantsBusiness接口", description = "XMerchantsBusiness接口")
public class XMerchantsBusinessController {

    @Resource
    private XMerchantsBusinessService xMerchantsBusinessService;

    /**
     * 1-增加
     *
     * @param dto 前端请求dto
     * @return 查询结果
     */
    @Operation(summary = "新增")
    @ApiResponse(responseCode = "200", description = "新增成功", content = @Content(schema = @Schema(implementation = XMerchantsBusiness.class)))
    @PostMapping("/add")
    public ResultEntity add(@RequestBody XMerchantsBusinessDTO dto) {
        log.info("新增数据参数：{}", JSONUtil.toJsonStr(dto));
        return xMerchantsBusinessService.add(dto);
    }

    /**
     * 2-删除
     *
     * @param dto 前端请求dto
     * @return 查询结果
     */
    @Operation(summary = "删除", description = "删除")
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = XMerchantsBusiness.class)))
    @PostMapping("/delete")
    public ResultEntity delete(@RequestBody XMerchantsBusinessDTO dto) {
        log.info("更新数据参数：{}", JSONUtil.toJsonStr(dto));
        return xMerchantsBusinessService.delete(dto);
    }

    /**
     * 3-修改
     *
     * @param dto 前端请求VO
     * @return 查询结果
     */
    @Operation(summary = "更新接口", description = "更新数据")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = XMerchantsBusiness.class)))
    @PostMapping("/update")
    public ResultEntity update(@RequestBody XMerchantsBusinessDTO dto) {
        log.info("更新数据参数：{}", JSONUtil.toJsonStr(dto));
        return xMerchantsBusinessService.update(dto);
    }

    /**
     * 4-查询-列表
     *
     * @param dto 列表搜索
     * @return 列表
     */
    @Operation(summary = "获取列表")
    @ApiResponse(responseCode = "200", description = "获取成功", content = @Content(schema = @Schema(implementation = XMerchantsBusiness.class)))
    @PostMapping(value = "/list")
    public ResultEntity list(@RequestBody XMerchantsBusinessDTO dto) {
        log.info("列表查询参数：{}", JSONUtil.toJsonStr(dto));
        return xMerchantsBusinessService.list(dto);
    }


    /**
     * 4.1-查询-详情
     *
     * @param dto
     * @return 基本
     */
    @Parameter(name = "xMerchantsBusinessId", description = "Id", required = true)
    @Operation(summary = "列表-点击详情", description = "详情数据")
    @ApiResponse(responseCode = "200", description = "详情", content = @Content(schema = @Schema(implementation = XMerchantsBusiness.class)))
    @PostMapping("/detail")
    public ResultEntity detail(@RequestBody XMerchantsBusinessDTO dto) {
        log.info("查询详情参数：{}", JSONUtil.toJsonStr(dto));
        return ResultEntity.success(xMerchantsBusinessService.detail(dto));
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
        xMerchantsBusinessService.importFile(multipartFile, "1", response);
    }

    /**
     * 5.1-下载导入模板
     */
    @Operation(summary = "模版下载接口", description = "模版下载接口")
    @ApiResponse(responseCode = "200", description = "模版下载接口")
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        xMerchantsBusinessService.downloadTemplate(response);
    }

    /**
     * 6-导出
     */
    @Operation(summary = "导出", description = "导出")
    @PostMapping("/export")
    public void export(@RequestBody XMerchantsBusinessDTO dto, HttpServletResponse response) throws Exception {
        log.info("导出的查询参数：{}", JSONUtil.toJsonStr(dto));
        xMerchantsBusinessService.exportFile(dto, response);
    }
}
