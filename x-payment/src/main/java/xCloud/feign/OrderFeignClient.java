package xCloud.feign;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description 通过 Feign 调用订单服务
 * @Author Andy Fan
 * @Date 2025/4/11 15:12
 * @ClassName OrderFeignClient
 */
@Tag(name = "OrderFeignClient", description = "通过 Feign 调用订单服务")
@FeignClient(name = "x-order") // 假设订单服务注册名为 x-order
public interface OrderFeignClient {

    @Operation(summary = "updateOrderStatus", description = "更新订单状态")
    @PostMapping("/order/updateStatus")
    String updateOrderStatus(
            @RequestParam("orderId") String orderId,
            @RequestParam("status") String status,
            @RequestParam("transactionId") String transactionId);
}
