package xCloud.feign;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description 定义供其他服务调用的接口--通过 Feign 调用payment服务
 * @Author Andy Fan
 * @Date 2025/4/11 15:11
 * @ClassName PaymentFeignClient
 */
@Tag(name = "PaymentFeignClient", description = "定义供其他服务调用的接口--通过 Feign 调用payment服务")
@FeignClient(name = "x-payment")
public interface PaymentFeignClient {

    @Operation(summary = "pay", description = "pay")
    @GetMapping("/alipay/pay")
    String pay(@RequestParam("orderId") String orderId,
               @RequestParam("amount") String amount,
               @RequestParam("subject") String subject);
}
