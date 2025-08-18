package xCloud.aRedis.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xCloud.aRedis.entity.User;
import xCloud.aRedis.entity.request.LimitRequest;
import xCloud.aRedis.entity.vo.LimitVo;
import xCloud.entity.ResultEntity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * @Description 测试redis lua脚步
 * @Author Andy Fan
 * @Date 2025/8/18 10:32
 * @ClassName ARedisService
 */
@Service
@Slf4j
public class ARedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    //每日限制数量
    private static final int DAILY_LIMIT = 30;
    //自营团队ID
    private static final int SELF_OPERATED_TEAM_ID = 1000000;

    /**
     * B联系C次数限制
     *
     * @param request request
     * @return ResultEntity
     */
    public ResultEntity<LimitVo> bConnectCLimit(LimitRequest request) {
        LimitVo limitVo = new LimitVo();
        limitVo.setIsLimit(isLimit(request.getBUserId(), request.getCUserId()));//限制
        return ResultEntity.success(limitVo);
    }

    /**
     * 判断是否限制
     *
     * @param bUserId bUserId
     * @param cUserId cUserId
     * @return 1：不限制 0：限制
     */
    public Integer isLimit(Integer bUserId, Integer cUserId) {
        //判断是否自营 srzpServiceClient.getGroupIdByUserId(bUserId)
        if (bUserId == SELF_OPERATED_TEAM_ID) {
            return 1;
        }
        //设置KEY
        String key = "b_connect_c_limit:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ":" + bUserId;
        //1-判断是否已联系过
        Boolean isMember = redisTemplate.opsForSet().isMember(key, String.valueOf(cUserId));
        if (Boolean.TRUE.equals(isMember)) {
            return 1; // 今天已联系过，不限制
        }
        //2-获取当前联系数量
        Long count = redisTemplate.opsForSet().size(key);
        if (count != null && count >= DAILY_LIMIT) {
            return 0; // 限制
        }
        //3-添加新的联系记录
        redisTemplate.opsForSet().add(key, String.valueOf(cUserId));

        //4-如果是第一次使用这个key，设置过期时间为当天剩余秒数
        if (count != null && count == 0) {
            redisTemplate.expire(key, getSecondsUntilMidnight(), TimeUnit.SECONDS);
        }
        return 1;
    }

    /**
     * 获取当前时间到明天零点的秒数
     *
     * @return 秒数
     */
    private long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight).getSeconds();
    }

}
