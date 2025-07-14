package xCloud.util;

import com.alibaba.fastjson.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;


import java.security.InvalidKeyException;

import org.apache.commons.codec.binary.Base64;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/7/7 13:34
 * @ClassName TestEEE
 */
public class ESignUtil {

//    post
//    https://esignoss.esign.cn/1111564182/12d88a2a-9999-4d70-94ef-19f78c7d7d97/tmp_ee57fbe29b3fcb77ac76b5f95f28d8cf141058210.jpg?Expires=1751945054&OSSAccessKeyId=STS.NY6f2b2ncYGbK7pAnjvRTphYe&Signature=d0e1MQNa2xI9oUPzhPmeH490fNI%3D&callback-var=eyJ4OmZpbGVfa2V5IjoiJDkxNzQ2ODFmLTUxNzktNDcwMC05NDgyLWJiMzYzOTJjM2M1OCQyNjQ0NTQwOTQ1In0%3D%0A&callback=eyJjYWxsYmFja1VybCI6Imh0dHA6Ly9zbWx0YXBpLnRzaWduLmNuL2FueWRvb3IvZmlsZS1zeXN0ZW0vY2FsbGJhY2svYWxpb3NzIiwiY2FsbGJhY2tCb2R5IjogIntcIm1pbWVUeXBlXCI6JHttaW1lVHlwZX0sXCJzaXplXCI6ICR7c2l6ZX0sXCJidWNrZXRcIjogJHtidWNrZXR9LFwib2JqZWN0XCI6ICR7b2JqZWN0fSxcImV0YWdcIjogJHtldGFnfSxcImZpbGVfa2V5XCI6JHt4OmZpbGVfa2V5fX0iLCJjYWxsYmFja0JvZHlUeXBlIjogImFwcGxpY2F0aW9uL2pzb24ifQ%3D%3D%0A&security-token=CAISxAJ1q6Ft5B2yfSjIr5qDLYjW37FC7oWJSRHBpW4%2Fet14n63yhzz2IHtKdXRvBu8Xs%2F4wnmxX7f4YlqB6T55OSAmcNZEoGHPsWJX7MeT7oMWQweEurv%2FMQBqyaXPS2MvVfJ%2BOLrf0ceusbFbpjzJ6xaCAGxypQ12iN%2B%2Fm6%2FNgdc9FHHPPD1x8CcxROxFppeIDKHLVLozNCBPxhXfKB0ca0WgVy0EHsPnvm5DNs0uH1AKjkbRM9r6ceMb0M5NeW75kSMqw0eBMca7M7TVd8RAi9t0t1%2FIVpGiY4YDAWQYLv0rda7DOltFiMkpla7MmXqlft%2BhzcgeQY0pc%2FW6e6mGuXYk9O0y3LOgrMNTZhjW%2BGPyN1KcpSpXcU%2B3%2Bp1TPyXcAS1g12v00dZTvxLH%2Bh%2BJCYs8X58VMgjO1iX9IIPPtACKu7MQdpz0agAFfUBCL25W3Te3ELz8tMn%2FJsp7pm5Jouo%2FPJqL2C3KCSYlk5zVF2SWuBDgWI%2B2AhJhl3vtPPUuivRbyev0FA8I6y6nRFdeGdPpSRsKjjOe769SPXfCOdiAObFtRAnrHo6Na%2BCr9qoVL0caDz0U8JB4n9r0svIc6blPaXBIPYE%2FJ4CAA

    //    url---https://open.esign.cn/doc/opendoc/dev-guide3/tggw2e
    // 应用ID
    public static final String EsignAppId = "7439071076";
    // 应用密钥
    public static final String EsignAppSecret = "c91c291f684d4eac5597d093e138f5ee";
    // e签宝接口调用域名（模拟环境）
    public static final String EsignHost = "https://smlopenapi.esign.cn";


    public static void main(String[] args) {
        // 应用ID
        String appId = EsignAppId;
        // 应用密钥
        String appKey = EsignAppSecret;
        // e签宝接口调用域名（模拟环境）
        String host = "https://smlopenapi.esign.cn";
        // e签宝接口调用域名（正式环境）
        //  String host = "https://openapi.esign.cn";

        // 请求签名鉴权-POST请求
        testPost(appId, appKey, host);

        // 请求签名鉴权-GET请求
        //  String signFlowId = "e622498****ebf72d57dbb";
        //  testGet(appId, appKey, host, signFlowId);

    }


    /***
     * 请求签名鉴权-POST请求
     *
     * @param appId
     * @param appKey
     * @param host
     */
    public static void testPost(String appId, String appKey, String host) {
        // 计算签名拼接的url
        String postUrl = "/v3/files/file-upload-url";
        // 完整的请求地址
        String postAllUrl = host + postUrl;
        try {
            // 构建请求Body体
            JSONObject reqBodyObj = new JSONObject();
            reqBodyObj.put("contentMd5", "KMYh+0qU9/FDXf2TwCGbeg==");
            reqBodyObj.put("contentType", "application/octet-stream");
            reqBodyObj.put("convertToPDF", "true");
            reqBodyObj.put("fileName", "销售合同.docx");
            reqBodyObj.put("fileSize", "81825");
            //reqBodyObj.put("convertToHTML", "false");


            // 请求Body体数据
//            String reqBodyData = reqBodyObj.toString();
            String reqBodyData = "{\"contentMd5\":\"OEsbf9ASKtmraNUObAexPA==\",\"fileName\":\"滴滴电子发票141039984.pdf\",\"fileSize\":\"75688\",\"convertToPDF\":\"false\",\"contentType\":\"application/octet-stream\"}";
            // 对请求Body体内的数据计算ContentMD5
            String contentMD5 = doContentMD5(reqBodyData);
            System.out.println("请求body数据:" + reqBodyData);
            System.out.println("body的md5值：" + contentMD5);

            // 构建待签名字符串
            String method = "POST";
            String accept = "*/*";
            String contentType = "application/json";
            String url = postUrl;
            String date = "";
            String headers = "";

            StringBuffer sb = new StringBuffer();
            sb.append(method).append("\n").append(accept).append("\n").append(contentMD5).append("\n")
                    .append(contentType).append("\n").append(date).append("\n");
            if ("".equals(headers)) {
                sb.append(headers).append(url);
            } else {
                sb.append(headers).append("\n").append(url);
            }

            // 构建参与请求签名计算的明文
            String plaintext = sb.toString();
            // 计算请求签名值
            String reqSignature = doSignatureBase64(plaintext, appKey);
            System.out.println("计算请求签名值:" + reqSignature);

            // 获取时间戳(精确到毫秒)
            long timeStamp = timeStamp();

            // 构建请求头
            LinkedHashMap<String, String> header = new LinkedHashMap<String, String>();
            header.put("X-Tsign-Open-App-Id", appId);
            header.put("X-Tsign-Open-Auth-Mode", "Signature");
            header.put("X-Tsign-Open-Ca-Timestamp", String.valueOf(timeStamp));
            header.put("Accept", accept);
            header.put("Content-Type", contentType);
            header.put("X-Tsign-Open-Ca-Signature", reqSignature);
            header.put("Content-MD5", contentMD5);

            // 发送POST请求
            System.out.println("-----发送POST请求");

            String result = HTTPHelper.sendPOST(postAllUrl, reqBodyData, header, "UTF-8");
            JSONObject resultObj = JSONObject.parseObject(result);
            System.out.println("请求返回信息： " + resultObj.toString());
            System.out.println("请求返回状态码： " + resultObj.getString("code"));
            System.out.println("请求返回结果： " + resultObj.getString("message"));
            System.out.println("请求返回数据： " + resultObj.getString("data"));
            System.out.println("-----done");
        } catch (Exception e) {
            e.printStackTrace();
            String msg = MessageFormat.format("请求签名鉴权方式调用接口出现异常: {0}", e.getMessage());
            System.out.println(msg);
        }
    }

    /***
     * 请求签名鉴权-GET请求
     *
     * @param appId
     * @param appKey
     * @param host
     */

    public static void testGet(String appId, String appKey, String host, String signFlowId) {
        // 计算签名拼接的url
        String getUrl = "/v3/sign-flow/" + signFlowId + "/detail";
        // 完整的请求地址
        String getAllUrl = host + getUrl;

        try {
            // GET请求时ContentMD5为""
            String contentMD5 = "";

            // 构建待签名字符串
            String method = "GET";
            String accept = "*/*";
            String contentType = "application/json; charset=UTF-8";
            String url = getUrl;
            String date = "";
            String headers = "";

            StringBuffer sb = new StringBuffer();
            sb.append(method).append("\n").append(accept).append("\n").append(contentMD5).append("\n")
                    .append(contentType).append("\n").append(date).append("\n");
            if ("".equals(headers)) {
                sb.append(headers).append(url);
            } else {
                sb.append(headers).append("\n").append(url);
            }

            // 构建参与请求签名计算的明文
            String plaintext = sb.toString();
            // 计算请求签名值
            String reqSignature = doSignatureBase64(plaintext, appKey);
            System.out.println("计算请求签名值：" + reqSignature);
            // 获取时间戳(精确到毫秒)
            long timeStamp = timeStamp();

            // 构建请求头
            LinkedHashMap<String, String> header = new LinkedHashMap<String, String>();
            header.put("X-Tsign-Open-App-Id", appId);
            header.put("X-Tsign-Open-Auth-Mode", "Signature");
            header.put("X-Tsign-Open-Ca-Timestamp", String.valueOf(timeStamp));
            header.put("Accept", accept);
            header.put("Content-Type", contentType);
            header.put("X-Tsign-Open-Ca-Signature", reqSignature);
            header.put("Content-MD5", contentMD5);

            HashMap<String, Object> query = new HashMap<String, Object>();
            // query.put("orgIDCardNum", "9100*****0004");
            // query.put("orgIDCardType", "CRED_ORG_USCC");


            // 发送GET请求
            String result = HTTPHelper.sendGet(getAllUrl, query, header, "UTF-8");
            JSONObject resultObj = JSONObject.parseObject(result);
            System.out.println("请求返回信息： " + resultObj.toString());
        } catch (Exception e) {
            e.printStackTrace();
            String msg = MessageFormat.format("请求签名鉴权方式调用接口出现异常: {0}", e.getMessage());
            System.out.println(msg);
        }
    }


    /***
     *
     * @param str 待计算的消息
     * @return MD5计算后摘要值的Base64编码(ContentMD5)
     * @throws Exception 加密过程中的异常信息
     */
    public static String doContentMD5(String str) throws Exception {
        byte[] md5Bytes = null;
        MessageDigest md5 = null;
        String contentMD5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md5.update(str.getBytes("UTF-8"));
            // 获取文件MD5的二进制数组（128位）
            md5Bytes = md5.digest();
            // 把MD5摘要后的二进制数组md5Bytes使用Base64进行编码（而不是对32位的16进制字符串进行编码）
            contentMD5 = new String(Base64.encodeBase64(md5Bytes), "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            String msg = MessageFormat.format("不支持此算法: {0}", e.getMessage());
            Exception ex = new Exception(msg);
            ex.initCause(e);
            throw ex;
        } catch (UnsupportedEncodingException e) {
            String msg = MessageFormat.format("不支持的字符编码: {0}", e.getMessage());
            Exception ex = new Exception(msg);
            ex.initCause(e);
            throw ex;
        }
        return contentMD5;
    }

    /***
     * 计算请求签名值
     *
     * @param message 待计算的消息
     * @param secret 密钥
     * @return HmacSHA256计算后摘要值的Base64编码
     * @throws Exception 加密过程中的异常信息
     */
    public static String doSignatureBase64(String message, String secret) throws Exception {
        String algorithm = "HmacSHA256";
        Mac hmacSha256;
        String digestBase64 = null;
        try {
            hmacSha256 = Mac.getInstance(algorithm);
            byte[] keyBytes = secret.getBytes("UTF-8");
            byte[] messageBytes = message.getBytes("UTF-8");
            hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm));
            // 使用HmacSHA256对二进制数据消息Bytes计算摘要
            byte[] digestBytes = hmacSha256.doFinal(messageBytes);
            // 把摘要后的结果digestBytes使用Base64进行编码
            digestBase64 = new String(Base64.encodeBase64(digestBytes), "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            String msg = MessageFormat.format("不支持此算法: {0}", e.getMessage());
            Exception ex = new Exception(msg);
            ex.initCause(e);
            throw ex;
        } catch (UnsupportedEncodingException e) {
            String msg = MessageFormat.format("不支持的字符编码: {0}", e.getMessage());
            Exception ex = new Exception(msg);
            ex.initCause(e);
            throw ex;
        } catch (InvalidKeyException e) {
            String msg = MessageFormat.format("无效的密钥规范: {0}", e.getMessage());
            Exception ex = new Exception(msg);
            ex.initCause(e);
            throw ex;
        }
        return digestBase64;
    }

    /***
     * 获取时间戳(毫秒级)
     *
     * @return 毫秒级时间戳, 如 1578446909000
     */
    public static long timeStamp() {
        long timeStamp = System.currentTimeMillis();
        return timeStamp;
    }


}
