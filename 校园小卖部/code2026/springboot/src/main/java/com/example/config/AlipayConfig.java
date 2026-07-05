package com.example.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

@Configuration
public class AlipayConfig {

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.app-private-key}")
    private String appPrivateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Bean
    public AlipayClient alipayClient() {
        if (!isConfigured()) {
            System.out.println("【支付宝】沙箱未配置，使用模拟支付模式");
            return null;
        }
        disableSSLValidation();

        com.alipay.api.AlipayConfig config = new com.alipay.api.AlipayConfig();
        config.setServerUrl(gatewayUrl);
        config.setAppId(appId);
        config.setPrivateKey(appPrivateKey);
        config.setFormat("json");
        config.setCharset("UTF-8");
        config.setAlipayPublicKey(alipayPublicKey);
        config.setSignType("RSA2");
        config.setConnectTimeout(15000);
        config.setReadTimeout(30000);

        System.out.println("【支付宝】沙箱初始化成功");
        try {
            System.out.println("【支付宝】沙箱初始化成功");
            return new DefaultAlipayClient(config);
        } catch (Exception e) {
            System.err.println("【支付宝】初始化失败: " + e.getMessage());
            return null;
        }

    }
    private void disableSSLValidation() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            System.err.println("【支付宝】SSL配置异常: " + e.getMessage());
        }
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public boolean isConfigured() {
        return !"your_sandbox_app_id".equals(appId);
    }
}
