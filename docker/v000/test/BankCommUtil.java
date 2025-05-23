package com.jan.base.util.bankOfCommunications.test;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/5/20 14:56
 * @ClassName BankCommUtil
 */

import com.alibaba.fastjson.JSON;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

public class BankCommUtil {
    public static String doPost(String requestStr, String url, String netBankClientId, String operatorId, String certId) throws Exception {
        CloseableHttpClient client = BankCommHttpClient.createHttpClient(); // 使用配置了证书的 HttpClient
        HttpPost post = new HttpPost(url);

        // 构造请求数据（假设 JSON 格式，需根据银行接口文档调整）
        String requestBody = String.format(
                requestStr,
                netBankClientId, operatorId, certId
        );

        post.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
        post.setHeader("Content-Type", "application/json");
        try (CloseableHttpResponse response = client.execute(post)) {
            System.out.println("--------JSON.toJSONString(response)");
            System.out.println(JSON.toJSONString(response));
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            } else {
                System.err.println("请求失败，状态码: " + (response != null ? response.getStatusLine().getStatusCode() : "无响应"));
                return null;
            }
        } finally {
            client.close();
        }
    }

    public static void main(String[] args) {
        try {
            String url = "https://<ServerIp>:<ServerPort>/api/transaction"; // 替换为银行提供的 URL
            String netBankClientId = "9000007196";
            String operatorId = "00019";
            String certId = "350eda8c";

//            String response = doPost(url, netBankClientId, operatorId, certId);
//            if (response != null) {
//                System.out.println("响应: " + response);
//            } else {
//                System.err.println("无响应或请求失败");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}