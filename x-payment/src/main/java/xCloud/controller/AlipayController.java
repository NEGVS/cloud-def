package xCloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xCloud.service.AlipayService;

/**
 * @Description 支付控制器--提供支付接口
 * @Author Andy Fan
 * @Date 2025/4/11 15:04
 * @ClassName AlipayController
 */
@RestController
public class AlipayController {
    @Autowired
    private AlipayService alipayService;

    @GetMapping("/alipay/pay")
    public String pay(
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") String amount,
            @RequestParam("subject") String subject) throws Exception {
        return alipayService.createPayment(orderId, amount, subject);
    }
}
