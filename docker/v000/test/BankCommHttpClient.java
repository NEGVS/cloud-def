package com.jan.base.util.bankOfCommunications.test;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/5/20 14:57
 * @ClassName BankCommHttpClient
 */

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;

public class BankCommHttpClient {
    public static CloseableHttpClient createHttpClient() throws Exception {
        // 加载客户端证书
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("client-keystore.jks"), "keystore-password".toCharArray());

        // 加载信任存储（可选，如果已导入到默认 cacerts 可省略）
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream("/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home/jre/lib/security/cacerts"), "changeit".toCharArray());

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadKeyMaterial(keyStore, "keystore-password".toCharArray())
                .loadTrustMaterial(trustStore, null)
                .build();

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        return HttpClients.custom().setSSLSocketFactory(socketFactory).build();
    }
}