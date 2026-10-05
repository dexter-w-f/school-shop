package com.example.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import cn.hutool.json.JSONObject;
import com.example.config.AlipayConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AlipayService {

    /**
     * 未配置支付宝沙箱时 AlipayConfig#alipayClient 会返回 null，
     * 因此这里必须用 required=false，否则应用无法启动。
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private AlipayClient alipayClient;

    @Resource
    private AlipayConfig alipayConfig;

    /**
     * 校验支付宝异步通知签名（RSA2）。
     * 验签失败一律视为伪造回调。
     */
    public boolean verifyNotify(Map<String, String> params) {
        if (params == null || params.isEmpty() || !alipayConfig.isConfigured()) {
            return false;
        }
        if (params.get("sign") == null || params.get("sign_type") == null) {
            return false;
        }
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    "UTF-8",
                    params.get("sign_type"));
        } catch (Exception e) {
            System.err.println("【支付宝】回调验签异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 创建支付宝支付（生成二维码内容）
     */
    public String createPayment(String orderNo, String amount, String subject) {
        if (!alipayConfig.isConfigured() || alipayClient == null) {
            return null;
        }
        try {
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            JSONObject biz = new JSONObject();
            biz.set("out_trade_no", orderNo);
            biz.set("total_amount", amount);
            biz.set("subject", subject);
            request.setBizContent(biz.toString());
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                return response.getQrCode();
            }
            System.err.println("【支付宝】创建支付失败: " + response.getMsg() + " - " + response.getSubMsg());
            return null;
        } catch (Exception e) {
            System.err.println("【支付宝】创建支付异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 查询支付状态
     * @return "SUCCESS" 已支付, "WAITING" 等待支付, "FAIL" 支付失败, null 查询失败
     */
    public String queryPayment(String orderNo) {
        if (!alipayConfig.isConfigured() || alipayClient == null) {
            return null;
        }
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    return "SUCCESS";
                } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                    return "FAIL";
                } else {
                    return "WAITING";
                }
            }
            return "WAITING";
        } catch (Exception e) {
            System.err.println("【支付宝】查询支付异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 发起退款
     */
    public boolean refund(String orderNo, String amount) {
        if (!alipayConfig.isConfigured() || alipayClient == null) {
            return false;
        }
        try {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            JSONObject biz = new JSONObject();
            biz.set("out_trade_no", orderNo);
            biz.set("refund_amount", amount);
            request.setBizContent(biz.toString());
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                System.out.println("【支付宝】退款成功: " + orderNo + ", 金额: " + amount);
                return true;
            }
            System.err.println("【支付宝】退款失败: " + response.getMsg() + " - " + response.getSubMsg());
            return false;
        } catch (Exception e) {
            System.err.println("【支付宝】退款异常: " + e.getMessage());
            return false;
        }
    }
}