package xCloud.feignClient.paymentClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * FeignClient 调用支付微服务
 */
@FeignClient(name = "x-payment", url = "${nacos.discovery.server-addr:localhost:8848}")
public interface paymentClient {

    @GetMapping("/payment")
    boolean processPayment(@RequestParam("orderId") String orderId,
                           @RequestParam("amount") String amount,
                           @RequestParam("subject") String subject);
}
