package xCloud.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import xCloud.entity.merchants.Merchants;
import xCloud.entity.merchants.MerchantsDTO;
import xCloud.entity.merchants.MerchantsVO;
import xCloud.service.MerchantsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description Merchants接口
 * @Author Andy Fan
 * @Date 2025-04-09 09:38:50
 * @ClassName MerchantsController
 */
@RestController
@RequestMapping("biz/merchants")
@Slf4j
@Tag(name = "Merchants接口", description = "Merchants接口")
public class MerchantsController {

    @Resource
    private MerchantsService merchantsService;

    /**
     * 1-增加
     *
     * @param dto 前端请求dto
     * @return 查询结果
     */
    @Operation(summary = "1.新增")
    @Parameters(
            value = {
                    @Parameter(name = "merchantsName", description = "商户名称", required = true),
                    @Parameter(name = "merchantsCode", description = "商户编码", required = true),
                    @Parameter(name = "merchantsType", description = "商户类型", required = true),
            }
    )
    @ApiResponse(responseCode = "200", description = "新增成功", content = @Content(schema = @Schema(implementation = Merchants.class)))
    @PostMapping("/add")
    public ResultEntity<Merchants> add(@RequestBody Merchants dto) {
        log.info("新增数据参数：{}", JSONUtil.toJsonStr(dto));
        return merchantsService.add(dto);
    }

    /**
     * 2-删除
     *
     * @param dto 前端请求dto
     * @return 查询结果
     */
    @Operation(summary = "2.删除", description = "删除")
    //@Parameter(name = "merchant_id", description = "merchant_id", required = true)
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = Merchants.class)))
    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody MerchantsDTO dto) {
        log.info("更新数据参数：{}", JSONUtil.toJsonStr(dto));
        return merchantsService.delete(dto);
    }

    /**
     * 3-修改
     *
     * @param dto 前端请求VO
     * @return 查询结果
     */
    @Operation(summary = "3.更新", description = "更新数据")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Merchants.class)))
    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody MerchantsDTO dto) {
        log.info("更新数据参数：{}", JSONUtil.toJsonStr(dto));
        return merchantsService.update(dto);
    }

    /**
     * 4-查询-列表
     *
     * @param dto 列表搜索
     * @return 列表
     */
    @Operation(summary = "4.获取分页列表-只有商家")
    @ApiResponse(responseCode = "200", description = "获取成功", content = @Content(schema = @Schema(implementation = MerchantsVO.class)))
    @PostMapping(value = "/list")
    public ResultEntity<Page<Merchants>> list(@RequestBody MerchantsDTO dto) {
        log.info("列表查询参数：{}", JSONUtil.toJsonStr(dto));
        return merchantsService.listPage(dto);
    }

    /**
     * 4-查询-列表
     *
     * @param dto 列表搜索
     * @return 列表
     */
    @Operation(summary = "4.获取列表-包含商品")
    @ApiResponse(responseCode = "200", description = "获取成功", content = @Content(schema = @Schema(implementation = MerchantsVO.class)))
    @PostMapping(value = "/listMerchantAndProduct")
    public ResultEntity<List<MerchantsVO>> listMerchantAndProduct(@RequestBody MerchantsDTO dto) {
        log.info("列表查询参数：{}", JSONUtil.toJsonStr(dto));
        List<MerchantsVO> merchantsWithProducts = merchantsService.listMerchantAndProduct(dto);
        return ResultEntity.success(merchantsWithProducts);
    }

    /**
     * 4.1-查询-详情
     *
     * @param dto
     * @return 基本
     */
    @Parameter(name = "merchantsId", description = "Id", required = true)
    @Operation(summary = "列表-点击详情", description = "详情数据")
    @ApiResponse(responseCode = "200", description = "详情", content = @Content(schema = @Schema(implementation = Merchants.class)))
    @PostMapping("/detail")
    public ResultEntity<MerchantsVO> detail(@RequestBody MerchantsDTO dto) throws Exception {
        log.info("查询详情参数：{}", JSONUtil.toJsonStr(dto));
        return ResultEntity.success(merchantsService.detail(dto));
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
        merchantsService.importFile(multipartFile, null, response);
    }

    /**
     * 5.1-下载导入模板
     */
    @Operation(summary = "模版下载接口", description = "模版下载接口")
    @ApiResponse(responseCode = "200", description = "模版下载接口")
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        merchantsService.downloadTemplate(response);
    }

    /**
     * 6-导出
     */
    @Operation(summary = "导出", description = "导出")
    @PostMapping("/export")
    public void export(@RequestBody MerchantsDTO dto, HttpServletResponse response) throws Exception {
        log.info("导出的查询参数：{}", JSONUtil.toJsonStr(dto));
        merchantsService.exportFile(dto, response);
    }
}