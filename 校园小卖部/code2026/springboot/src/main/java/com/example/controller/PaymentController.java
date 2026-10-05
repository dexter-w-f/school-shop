package com.example.controller;

import com.example.common.Result;
import com.example.config.AuthValidator;
import com.example.service.AlipayService;
import com.example.service.OrdersService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private OrdersService ordersService;
    @Resource
    private AlipayService alipayService;
    @Resource
    private HttpServletRequest request;

    /**
     * 支付订单（余额支付/模拟支付）
     */
    @PostMapping("/pay")
    public Result pay(@RequestBody Map<String, Object> params) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        Integer orderId = (Integer) params.get("orderId");
        String payType = (String) params.get("payType");
        if (orderId == null || payType == null || payType.isBlank()) {
            return Result.error("参数异常");
        }
        boolean paid = ordersService.payByUser(orderId, payType, currentUserId);
        return paid ? Result.success() : Result.error("订单状态异常，支付失败");
    }

    /**
     * 创建支付宝支付，返回二维码内容（只能为自己的订单发起）
     */
    @GetMapping("/alipayPay")
    public Result alipayPay(@RequestParam Integer orderId) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        if (!isMyOrder(orderId, currentUserId)) {
            return Result.error("无权操作该订单");
        }
        Map<String, Object> result = ordersService.createAlipayPayment(orderId);
        if (result == null) {
            return Result.error("创建支付宝支付失败");
        }
        return Result.success(result);
    }

    /**
     * 查询订单支付状态（前端轮询用；只能查自己的订单）
     */
    @GetMapping("/queryStatus")
    public Result queryStatus(@RequestParam Integer orderId) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        if (!isMyOrder(orderId, currentUserId)) {
            return Result.error("无权查看该订单");
        }
        String status = ordersService.queryPaymentStatus(orderId);
        Map<String, String> result = new HashMap<>();
        result.put("status", status);
        return Result.success(result);
    }

    /**
     * 校验订单归属：仅允许订单本人操作。
     */
    private boolean isMyOrder(Integer orderId, Integer currentUserId) {
        if (orderId == null) {
            return false;
        }
        com.example.entity.Orders order = ordersService.selectById(orderId);
        return order != null && currentUserId.equals(order.getUserId());
    }

    /**
     * 支付宝异步通知回调（需外网可访问）
     */
    @PostMapping("/alipayNotify")
    public void alipayNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }
            boolean verified = ordersService.processAlipayNotify(params);
            PrintWriter out = response.getWriter();
            if (verified) {
                out.print("success");
            } else {
                out.print("failure");
            }
            out.flush();
        } catch (Exception e) {
            System.err.println("【支付宝】异步通知处理异常: " + e.getMessage());
        }
    }
}



