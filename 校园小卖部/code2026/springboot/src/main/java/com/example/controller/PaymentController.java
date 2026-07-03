package com.example.controller;

import com.example.common.Result;
import com.example.service.OrdersService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 支付接口
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private OrdersService ordersService;

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public Result pay(@RequestBody Map<String, Object> params) {
        Integer orderId = Integer.valueOf(params.get("orderId").toString());
        String payType = params.get("payType").toString();
        ordersService.pay(orderId, payType);
        return Result.success();
    }
}
