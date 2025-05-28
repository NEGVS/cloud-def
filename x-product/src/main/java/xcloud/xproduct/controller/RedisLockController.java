package xcloud.xproduct.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xcloud.xproduct.config.redis.RedisLockService;

/**
 * @Description test use  redis lock
 * @Author Andy Fan
 * @Date 2025/5/27 19:46
 * @ClassName RedisLockController
 */
@RestController
@RequestMapping("/redis")
public class RedisLockController {
    @Autowired
    private RedisLockService lockService;

    @RequestMapping("/testLock")
    public String testLock() throws InterruptedException {
        String lockValue = lockService.acquireLock();
        if (lockValue != null) {
            System.out.println("获取锁成功" + lockValue);
            // 模拟业务逻辑执行
            Thread.sleep(5000);
            if (lockService.releaseLock(lockValue)) {
                System.out.println("释放锁成功");
                return "success";
            } else {
                System.out.println("释放锁失败");
            }
        } else {
            System.out.println("获取锁失败");
            return "fail";
        }
        return "success";
    }

}
