package com.example.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.common.Result;
import cn.hutool.core.date.DateUtil;
import com.example.entity.Goods;
import com.example.entity.Orders;
import com.example.entity.ChatMessage;
import com.example.entity.User;
import com.example.mapper.GoodsMapper;
import com.example.mapper.ChatMessageMapper;
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
    @Resource
    private ChatMessageMapper chatMessageMapper;

    @PostMapping("/send")
    public Result send(@RequestBody Map<String, String> params, jakarta.servlet.http.HttpServletRequest request) {
        String question = params.get("question");
        String historyJson = params.get("history");
        // 用户身份一律取自登录会话，忽略请求体传入的 userId，避免被用来读取他人余额/订单
        Integer sessionUserId = com.example.config.AuthValidator.requireUserId(request);
        String userIdStr = sessionUserId == null ? null : String.valueOf(sessionUserId);
        if (question == null || question.trim().isEmpty()) {
            return Result.error("请输入问题");
        }

        // 获取当前在售商品列表
        Goods query = new Goods();
        query.setStatus("上架");
        List<Goods> goodsList = goodsMapper.selectAll(query);
        String goodsContext = goodsList.stream()
                .map(g -> "- " + g.getName() + " (￥" + g.getPrice() + ", 库存" + g.getStore() + ")")
                .collect(Collectors.joining(""+System.lineSeparator()+""));
        String systemPrompt = "你是一个校园小卖部的在线客服助手，叫\"小卖部助手\"。"
                + "你热情友好，回答简洁，帮学生解决商品咨询、订单问题、配送问题等。"
                + "回复控制在100字以内，口语化，适当用表情。如用户查询订单或账户信息，请根据下方提供的用户数据如实回答。"+System.lineSeparator()+""+System.lineSeparator()+""
                + "【店铺信息】"+System.lineSeparator()+"- 名称: 校园小卖部"+System.lineSeparator()+"- 营业时间: 每天 8:00-22:00"+System.lineSeparator()+"- 配送方式: 到店自提(免费)/ 外送(满20元免配送费)"+System.lineSeparator()+""
                + "- 地址: 校园内"+System.lineSeparator()+""+System.lineSeparator()+""
                + "【售后政策】"+System.lineSeparator()+"- 商品质量问题可退换"+System.lineSeparator()+"- 非质量问题不影响二次销售可退"+System.lineSeparator()+"- 退换请联系管理员"+System.lineSeparator()+""+System.lineSeparator()+""
                + "【在售商品】(请只推荐以下商品，不要推荐不存在商品):"+System.lineSeparator()+"" + goodsContext;

        // 获取用户上下文信息
        String userContext = "";
        if (userIdStr != null) {
            try {
                Integer userId = Integer.valueOf(userIdStr);
                User user = userMapper.selectById(userId);
                if (user != null) {
                    userContext += ""+System.lineSeparator()+""+System.lineSeparator()+"当前用户信息:"+System.lineSeparator()+"- 账户余额: ￥" + user.getAccount() + ""+System.lineSeparator()+"- 用户名: " + user.getName();

                    // 获取用户最近订单
                    Orders orderQuery = new Orders();
                    orderQuery.setUserId(userId);
                    List<Orders> orderList = ordersService.selectAll(orderQuery);
                    if (orderList != null && !orderList.isEmpty()) {
                        userContext += ""+System.lineSeparator()+"- 近期订单:"+System.lineSeparator()+"";
                        for (Orders o : orderList.stream().collect(Collectors.toList())) {
                            userContext += "  订单#" + o.getOrderNo() + ": " + o.getStatus() + ", ￥" + o.getTotal() + ", " + o.getTime() + ""+System.lineSeparator()+"";
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
                    .timeout(30000)
                    .execute()
                    .body();

            JSONObject json = new JSONObject(response);
            String reply = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

            ChatMessage msg = new ChatMessage();
            if (userIdStr != null) msg.setUserId(Integer.valueOf(userIdStr));
            msg.setQuestion(question);
            msg.setAnswer(reply);
            msg.setTime(DateUtil.now());
            chatMessageMapper.insert(msg);

            return Result.success(reply);
        } catch (Exception e) {
            System.err.println("[Chat] DeepSeek API错误: " + e.getMessage());
            try {
                ChatMessage msg = new ChatMessage();
                if (userIdStr != null) msg.setUserId(Integer.valueOf(userIdStr));
                msg.setQuestion(question);
                msg.setTime(DateUtil.now());
                chatMessageMapper.insert(msg);
            } catch (Exception ignored) {}
            return Result.error("客服暂时忙线，请稍后再试");
        }
    }
}







