package xCloud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 支付控制器--提供支付接口
 * @Author Andy Fan
 * @Date 2025/4/11 15:04
 * @ClassName AlipayController
 */
@Tag(name = "AlipayController", description = "AlipayController")
@RestController
@Slf4j
public class AlipayController {

    @Operation(summary = "pay", description = "pay")
    @GetMapping("/alipay/pay")
    public String pay(
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") String amount,
            @RequestParam("subject") String subject) throws Exception {
        log.info("\nPayment开始调用支付宝接口\n订单号: " + orderId + ", 金额: " + amount + ", 标题: " + subject + "\n支付成功");
        return "支付成功";
    }
}
