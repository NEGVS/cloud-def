package xCloud.aRedis.controller;

import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.aRedis.entity.request.LimitRequest;
import xCloud.aRedis.entity.vo.LimitVo;
import xCloud.aRedis.service.ARedisService;
import xCloud.entity.ResultEntity;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/8/18 10:31
 * @ClassName ARedisController
 */
@Tag(name = "ARedisController", description = "ARedisController")
@Slf4j
@RestController
@RequestMapping("/aRedis")
public class ARedisController {

    @Resource
    private ARedisService aRedisService;

    @Operation(summary = "aRedis")
    @PostMapping("/aRedis")
    public ResultEntity<LimitVo> aRedis(@RequestBody LimitRequest request) {
        log.info("\nprompt: {}", JSONUtil.toJsonStr(request));
        return aRedisService.bConnectCLimit(request);
    }
}
