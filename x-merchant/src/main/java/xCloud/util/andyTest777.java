package xCloud.util;


import cn.hutool.core.date.DateUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import xCloud.service.serviceImpl.CodeX;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/7/7 13:21
 * @ClassName andyTest777
 */
public class andyTest777 {

    // 应用ID
    public static final String EsignAppId = "7439071076";
    // 应用密钥
    public static final String EsignAppSecret = "c91c291f684d4eac5597d093e138f5ee";
    // e签宝接口调用域名（模拟环境）
    public static final String EsignHost = "https://smlopenapi.esign.cn";

    public static void main(String[] args) {
        System.out.println(CodeX.getDate_yyyy_MM_dd());
        System.out.println(CodeX.getDate_yyyy_MM_dd(DateUtil.tomorrow()));

        System.out.println(CodeX.getDate_yyyy_MM_dd("2025-07-08", 1).replace("-", ""));
        if (true) {
            return;
        }
        String bodyData = "{\"name\":\"张某人\",\"age\":18}";
        String bodyContentMD5 = getBodyContentMD5(bodyData);
        System.out.println("请求Body体Content-MD5值:\n" + bodyContentMD5);

        String HTTPMethod = "POST";
        String Accept = "*/*";
        String ContentMD5 = "uxydqKBMBy6x1siClKEQ6Q==";
        String ContentType = "application/json; charset=UTF-8";
        String Date = "";
        String Headers = "";
        String PathAndParameters = "/v3/sign-flow/create-by-file";
        // 组合拼接待签名字符串
        StringBuffer strBuff = new StringBuffer();
        strBuff.append(HTTPMethod).append("\n").append(Accept).append("\n").append(ContentMD5).append("\n")
                .append(ContentType).append("\n").append(Date).append("\n");
        if ("".equals(Headers)) {
            strBuff.append(Headers).append(PathAndParameters);
        } else {
            strBuff.append(Headers).append("\n").append(PathAndParameters);
        }
//        字段之间使用\n间隔，若Headers为空，则不需要加\n，其他字段若为空都需要保留\n。注：大小写敏感。
        String StringToSign = strBuff.toString();
        System.out.println("待签名字符串:\n" + StringToSign);
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(EsignHost);
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.addHeader("X-Api-Sign-Version", "2.0.0");
            // 创建参数列表
//            if (param != null) {
//                List<NameValuePair> paramList = new ArrayList<>();
//                for (String key : param.keySet()) {
//                    paramList.add(new BasicNameValuePair(key, param.get(key)));
//                }
//                // 模拟表单
//                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8);
//                httpPost.setEntity(entity);
//            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * 计算请求Body体的Content-MD5
     * @param bodyData 请求Body体数据
     * @return
     */
    public static String getBodyContentMD5(String bodyData) {
        // 获取Body体的MD5的二进制数组（128位）
        byte[] bytes = getBodyMD5Bytes128(bodyData);
        // 对Body体MD5的二进制数组进行Base64编码

        return new String(Base64.encodeBase64(bytes));
    }

    /***
     * 获取MD5-二进制数组（128位）
     *
     * @param bodyData 请求Body体数据
     * @return
     */
    public static byte[] getBodyMD5Bytes128(String bodyData) {
        byte[] md5Bytes = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(bodyData.getBytes(StandardCharsets.UTF_8));
            md5Bytes = md5.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5Bytes;
    }
}
