package xCloud.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xCloud.feign.OrderFeignClient;

import java.util.Map;

/**
 * @Description 回调控制器--处理支付回调
 * @Author Andy Fan
 * @Date 2025/4/11 15:08
 * @ClassName AlipayNotifyController
 */
@Tag(name = "AlipayNotifyController", description = "AlipayNotifyController")
@RestController
public class AlipayNotifyController {

    @Value("${alipay.public-key}")
    private String publicKey;


    @Autowired
    private OrderFeignClient orderFeignClient;
    @Operation(summary = "pay", description = "pay")
    @PostMapping("/alipay/notify")
    public String handleNotify(@RequestParam Map<String, String> params) throws AlipayApiException {
        boolean signVerified = AlipaySignature.rsaCheckV1(params, publicKey, "UTF-8", "RSA2");
        if (!signVerified) {
            return "fail";
        }

        String tradeStatus = params.get("trade_status");
        String orderId = params.get("out_trade_no");
        String alipayTradeNo = params.get("trade_no");

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // TODO: 更新订单状态（可通过事件或 Feign 通知订单服务）
            // 通过 Feign 调用订单服务更新状态
            String result = orderFeignClient.updateOrderStatus(orderId, "PAID", alipayTradeNo);

            System.out.println("支付成功 - 订单号: " + orderId + ", 支付宝交易号: " + alipayTradeNo);

            if ("success".equals(result)) {
                System.out.println("订单 " + orderId + " 支付成功，支付宝交易号: " + alipayTradeNo);
                return "success";
            } else {
                System.out.println("订单状态更新失败: " + orderId);
                return "fail";
            }

//            return "success";
        }
        return "fail";
    }
}
