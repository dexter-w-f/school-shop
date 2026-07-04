package com.example.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.common.Result;
import cn.hutool.core.date.DateUtil;
import com.example.entity.Goods;
import com.example.entity.Orders;
import com.example.entity.User;
import com.example.mapper.GoodsMapper;
import com.example.mapper.UserMapper;
import com.example.service.OrdersService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 在线客服接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    private String apiKey;

    @Value("${ai.api-key}")
    public void setApiKey(String key) { this.apiKey = key; }

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OrdersService ordersService;

    @PostMapping("/send")
    public Result send(@RequestBody Map<String, String> params) {
        String question = params.get("question");
        String historyJson = params.get("history");
        String userIdStr = params.get("userId");
        if (question == null || question.trim().isEmpty()) {
            return Result.error("请输入问题");
        }

        // 获取当前在售商品列表
        Goods query = new Goods();
        query.setStatus("上架");
        List<Goods> goodsList = goodsMapper.selectAll(query);
        String goodsContext = goodsList.stream()
                .map(g -> "- " + g.getName() + " (￥" + g.getPrice() + ", 库存" + g.getStore() + ")")
                .collect(Collectors.joining("\n"));
        String systemPrompt = "你是一个校园小卖部的在线客服助手，叫\"小卖部助手\"。"
                + "你热情友好，回答简洁，帮学生解决商品咨询、订单问题、配送问题等。"
                + "回复控制在100字以内，口语化，适当用表情。如用户查询订单或账户信息，请根据下方提供的用户数据如实回答。\n\n"
                + "【店铺信息】\n- 名称: 校园小卖部\n- 营业时间: 每天 8:00-22:00\n- 配送方式: 到店自提（免费）/ 外送（满20元免配送费）\n"
                + "- 地址: 校园内\n\n"
                + "【售后政策】\n- 商品质量问题可退换\n- 非质量问题不影响二次销售可退\n- 退换请联系管理员\n\n"
                + "【在售商品】（请只推荐以下商品，不要推荐不存在商品）:\n" + goodsContext;

        // 获取用户上下文信息
        String userContext = "";
        if (userIdStr != null) {
            try {
                Integer userId = Integer.valueOf(userIdStr);
                User user = userMapper.selectById(userId);
                if (user != null) {
                    userContext += "\n\n当前用户信息:\n- 账户余额: ￥" + user.getAccount() + "\n- 用户名: " + user.getName();

                    // 获取用户最近订单
                    Orders orderQuery = new Orders();
                    orderQuery.setUserId(userId);
                    List<Orders> orderList = ordersService.selectAll(orderQuery);
                    if (orderList != null && !orderList.isEmpty()) {
                        userContext += "\n- 近期订单:\n";
                        for (Orders o : orderList.stream().limit(5).collect(Collectors.toList())) {
                            userContext += "  订单#" + o.getOrderNo() + ": " + o.getStatus() + ", ￥" + o.getTotal() + ", " + o.getTime() + "\n";
                        }
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

        systemPrompt += userContext;

        // 构建 DeepSeek API 请求
        JSONObject body = new JSONObject();
        body.set("model", "deepseek-chat");
        body.set("stream", false);

        // 消息数组: 系统提示词 + 历史对话 + 当前问题
        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
        if (historyJson != null) {
            JSONArray history = new JSONArray(historyJson);
            for (int i = 0; i < history.size(); i++) {
                messages.add(history.getJSONObject(i));
            }
        }
        messages.add(new JSONObject().set("role", "user").set("content", question));
        body.set("messages", messages);

        try {
            String response = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(15000)
                    .execute()
                    .body();

            JSONObject json = new JSONObject(response);
            String reply = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

            return Result.success(reply);
            ChatMessage msg = new ChatMessage();
            if (userIdStr != null) msg.setUserId(Integer.valueOf(userIdStr));
            msg.setQuestion(question); msg.setAnswer(reply); msg.setTime(DateUtil.now());
            chatMessageMapper.insert(msg);
        } catch (Exception e) {
            try { ChatMessage msg = new ChatMessage();
                if (userIdStr != null) msg.setUserId(Integer.valueOf(userIdStr));
                msg.setQuestion(question); msg.setTime(DateUtil.now());
                chatMessageMapper.insert(msg);
            } catch (Exception ignored) {}
            return Result.error("客服暂时忙线，请稍后再试");
        }
    }
}

