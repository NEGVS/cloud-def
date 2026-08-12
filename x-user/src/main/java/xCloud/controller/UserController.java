package xCloud.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.entity.LoginDTO;
import xCloud.entity.LoginVO;
import xCloud.entity.ResultEntity;
import xCloud.entity.UserDTO;
import xCloud.entity.UserVO;
import xCloud.service.UserService;

/**
 * @Description User接口
 * @Author Andy Fan
 * @Date 2025-04-07 10:51:53
 * @ClassName UserController
 */
@RestController
@RequestMapping("biz/user")
@Slf4j
@Tag(name = "User接口", description = "用户登录与CRUD接口")
public class UserController {

    @Resource
    private UserService userService;

    // ==========【登录：账号密码 -> JWT令牌】==========
    @Operation(summary = "用户登录", description = "校验账号密码，成功返回JWT令牌")
    @PostMapping("/login")
    public ResultEntity<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        log.info("登录请求：userName={}", dto.getUserName());
        String loginIp = JakartaServletUtil.getClientIP(request);
        return ResultEntity.success(userService.login(dto, loginIp), "登录成功");
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID查询详情，不含密码")
    @GetMapping("/{id}")
    public ResultEntity<UserVO> getUser(@PathVariable("id") Long userId) {
        return ResultEntity.success(userService.getUser(userId));
    }

    @Operation(summary = "新增用户", description = "新增用户，密码BCrypt加密存储")
    @PostMapping("/add")
    public ResultEntity<Long> add(@RequestBody UserDTO dto) {
        log.info("新增用户参数：{}", JSONUtil.toJsonStr(dto));
        return ResultEntity.success(userService.add(dto), "新增成功");
    }

    @Operation(summary = "删除用户", description = "逻辑删除")
    @PostMapping("/delete/{id}")
    public ResultEntity<Boolean> delete(@PathVariable("id") Long userId) {
        log.info("删除用户：userId={}", userId);
        return ResultEntity.success(userService.delete(userId), "删除成功");
    }

    @Operation(summary = "更新用户", description = "更新用户信息，不含密码")
    @PostMapping("/update")
    public ResultEntity<Boolean> update(@RequestBody UserDTO dto) {
        log.info("更新用户参数：{}", JSONUtil.toJsonStr(dto));
        return ResultEntity.success(userService.update(dto), "更新成功");
    }

    @Operation(summary = "分页查询用户列表", description = "按条件分页查询，不含密码")
    @PostMapping("/list")
    public ResultEntity<IPage<UserVO>> list(@RequestBody UserDTO dto) {
        log.info("列表查询参数：{}", JSONUtil.toJsonStr(dto));
        return ResultEntity.success(userService.list(dto));
    }
}
