package xCloud.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Description 支付服务类--实现支付逻辑
 * @Author Andy Fan
 * @Date 2025/4/11 15:02
 * @ClassName AlipayService
 */
@Slf4j
@Service
public class AlipayService {

    @Autowired
    private AlipayClient alipayClient;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    public String createPayment(String orderId, String amount, String subject) throws Exception {
        log.info("\n----------创建支付宝支付订单");
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(notifyUrl);

        String bizContent = "{"
                + "\"out_trade_no\":\"" + orderId + "\","
                + "\"total_amount\":\"" + amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"product_code\":\"FACE_TO_FACE_PAYMENT\""
                + "}";
        request.setBizContent(bizContent);

        AlipayTradePrecreateResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            log.info("\n-----------返回二维码链接:"+response.getQrCode());
            return response.getQrCode();
        } else {
            throw new RuntimeException("支付创建失败: " + response.getSubMsg());
        }
    }
}
