package com.example.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.example.common.Result;
import com.example.entity.Goods;
import com.example.mapper.GoodsMapper;
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

    @PostMapping("/send")
    public Result send(@RequestBody Map<String, String> params) {
        String question = params.get("question");
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
                + "回答控制在100字以内。\n\n"
                + "当前店铺在售商品如下（请只推荐以下商品，不要推荐不在列表中的商品）:\n" + goodsContext;

        // 构建 DeepSeek API 请求
        JSONObject body = new JSONObject();
        body.set("model", "deepseek-chat");
        body.set("stream", false);

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
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
        } catch (Exception e) {
            return Result.error("客服暂时忙线，请稍后再试");
        }
    }
}
