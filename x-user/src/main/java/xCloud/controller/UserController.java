package xCloud.controller;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.entity.User;
import xCloud.entity.UserDTO;
import xCloud.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description User接口
 * @Author Andy Fan
 * @Date 2025-04-07 10:51:53
 * @ClassName UserController
 */
@RestController
@RequestMapping("biz/user")
@Slf4j
@Tag(name = "User接口", description = "User接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 1-增加
     *
     * @param dto 前端请求dto
     * @return 查询结果
     */
    @Operation(summary = "新增")
    @Parameters(
            value = {
                    @Parameter(name = "userId", description = "Id", required = true),
                    @Parameter(name = "userName", description = "姓名", required = true),
                    @Parameter(name = "userAge", description = "年龄", required = true),
                    @Parameter(name = "userAddress", description = "地址", required = true),
                    @Parameter(name = "userPhone", description = "手机号", required = true),
            }
    )
    @ApiResponse(responseCode = "200", description = "新增成功", content = @Content(schema = @Schema(implementation = User.class)))
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody UserDTO dto) {
        log.info("新增数据参数：{}", JSONUtil.toJsonStr(dto));
        return userService.add(dto);
    }

    /**
     * 2-删除
     *
     * @param dto 前端请求dto
     * @return 查询结果
     */
    @Operation(summary = "删除", description = "删除")
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = User.class)))
    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody UserDTO dto) {
        log.info("更新数据参数：{}", JSONUtil.toJsonStr(dto));
        return userService.delete(dto);
    }

    /**
     * 3-修改
     *
     * @param dto 前端请求VO
     * @return 查询结果
     */
    @Operation(summary = "更新接口", description = "更新数据")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = User.class)))
    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody UserDTO dto) {
        log.info("更新数据参数：{}", JSONUtil.toJsonStr(dto));
        return userService.update(dto);
    }

    /**
     * 4-查询-列表
     *
     * @param dto 列表搜索
     * @return 列表
     */
    @Operation(summary = "获取列表")
    @ApiResponse(responseCode = "200", description = "获取成功", content = @Content(schema = @Schema(implementation = User.class)))
    @PostMapping(value = "/list")
    public Map<String, Object> list(@RequestBody UserDTO dto) {
        log.info("列表查询参数：{}", JSONUtil.toJsonStr(dto));
        Map<String, Object> resultMap = userService.list(dto);
        Object data = null;
        long total = 0;
        Object idNumberList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
            if (Objects.equals(entry.getKey(), "records")) {
                data = entry.getValue();
            }
            if (Objects.equals(entry.getKey(), "count")) {
                total = (long) entry.getValue();
            }
            if (Objects.equals(entry.getKey(), "idNumberList")) {
                idNumberList = entry.getValue();
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", data);
        map.put("notFoundIdNumberList", idNumberList);
        map.put("page", total);
        return null;
    }


    /**
     * 4.1-查询-详情
     *
     * @param dto
     * @return 基本
     */
    @Parameter(name = "userId", description = "Id", required = true)
    @Operation(summary = "列表-点击详情", description = "详情数据")
    @ApiResponse(responseCode = "200", description = "详情", content = @Content(schema = @Schema(implementation = User.class)))
    @PostMapping("/detail")
    public Map<String, Object> detail(@RequestBody UserDTO dto) throws Exception {
        log.info("查询详情参数：{}", JSONUtil.toJsonStr(dto));
        return null;
    }
//
//    /**
//     * 5-导入
//     *
//     * @param multipartFile 文件流
//     * @param response      响应流
//     */
//    @Operation(summary = "导入")
//    @Parameter(name = "uploadFile", description = "导入文件", required = true)
//    @ApiResponse(responseCode = "200", description = "导入成功")
//    @PostMapping("importFile")
//    public void importFile(@RequestParam("uploadFile") MultipartFile multipartFile, HttpServletResponse response) throws Exception {
////        userService.importFile(multipartFile, UserContext.getUser().getYsUserId(), response);
//    }
//
//    /**
//     * 5.1-下载导入模板
//     */
//    @Operation(summary = "模版下载接口", description = "模版下载接口")
//    @ApiResponse(responseCode = "200", description = "模版下载接口")
//    @GetMapping("/downloadTemplate")
//    public void downloadTemplate(HttpServletResponse response) throws Exception {
//        userService.downloadTemplate(response);
//    }
//
//    /**
//     * 6-导出
//     */
//    @Operation(summary = "导出", description = "导出")
//    @PostMapping("/export")
//    public void export(@RequestBody UserDTO dto, HttpServletResponse response) throws Exception {
//        log.info("导出的查询参数：{}", JSONUtil.toJsonStr(dto));
//        userService.exportFile(dto, response);
//    }
}
