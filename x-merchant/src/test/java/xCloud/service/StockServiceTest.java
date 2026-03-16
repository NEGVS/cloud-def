package xCloud.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.http.param.MediaType;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import xCloud.entity.Stock;
import xCloud.miniProgram.MiniProgramSubscribeMsgRequest;
import xCloud.util.HttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Resource
    private StockService stockService;

    @Test
    void test() {
        try {
            String openid = "o_TaI5LnbU2b-ylakaxTH3rqq-qk";
            String page = "index";
            String templateId = "bNQGAuX4xMyYgrIf-39EZRaW6dBEeNNTt6zEkQ7M8Dw";
            Map<String, String> data = new HashMap<>();
            data.put("thing1", "猜猜猜");
            data.put("time4", "2026-01-01");
            data.put("thing3", "高规格");

            //        sendMiniProgramSubscriptionMsg
            //构建请求URL（拼接AccessToken）
            String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=";
            String accessToken = "100_4RmpRKMqn35BGmTWY1SCl9zVrCl5DgiTh2bgFPXJ4ntp44C_OnLQ4l0iyln3w7d6-Sasd5_btFL5z6f7MRvbBPEguY3R-FdYoUcMeR4l5ObWmFwOu6HC22_Onl8XJPgABABNT";
            String finalSendUrl = sendMsgUrl + accessToken;
            //封装请求参数
            MiniProgramSubscribeMsgRequest request = new MiniProgramSubscribeMsgRequest();
            request.setTouser(openid);
            request.setTemplate_id(templateId);
            request.setPage(page);

            //转换数据格式（Map<String,String> → Map<String,WxTemplateData>）
            Map<String, MiniProgramSubscribeMsgRequest.MiniProgramTemplateData> templateData = new HashMap<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                MiniProgramSubscribeMsgRequest.MiniProgramTemplateData templateDataItem = new MiniProgramSubscribeMsgRequest.MiniProgramTemplateData();
                templateDataItem.setValue(entry.getValue());
                templateData.put(entry.getKey(), templateDataItem);
            }
            request.setData(templateData);

            HttpUtil.doPost(sendMsgUrl, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
