package xCloud.feignClient.userClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description FeignClient 调用用户服务
 * @Author Andy Fan
 * @Date 2025/7/7 14:03
 * @ClassName UserClient
 */
@FeignClient(name = "x-user", url = "${nacos.discovery.server-addr:localhost:8848}")
public interface UserClient {


    /**
     * 根据用户ID查询用户信息
     *
     * @param userId userId
     * @return User
     */
    @GetMapping("/user/{id}")
    User getUser(@PathVariable("id") Long userId);

}
