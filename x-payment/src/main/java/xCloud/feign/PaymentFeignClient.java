package xCloud.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description 定义供其他服务调用的接口--通过 Feign 调用订单服务
 * @Author Andy Fan
 * @Date 2025/4/11 15:11
 * @ClassName PaymentFeignClient
 */
@FeignClient(name = "payment-service")
public interface PaymentFeignClient {

    @GetMapping("/alipay/pay")
    String pay(@RequestParam("orderId") String orderId,
               @RequestParam("amount") String amount,
               @RequestParam("subject") String subject);
}
