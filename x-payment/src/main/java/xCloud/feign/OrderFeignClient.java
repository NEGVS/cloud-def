package xCloud.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description 通过 Feign 调用订单服务
 * @Author Andy Fan
 * @Date 2025/4/11 15:12
 * @ClassName OrderFeignClient
 */
@FeignClient(name = "order-service") // 假设订单服务注册名为 order-service
public interface OrderFeignClient {

    @PostMapping("/order/updateStatus")
    String updateOrderStatus(
            @RequestParam("orderId") String orderId,
            @RequestParam("status") String status,
            @RequestParam("transactionId") String transactionId);
}
