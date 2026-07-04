package com.example.controller;

import cn.hutool.core.util.RandomUtil;
import com.example.common.Result;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/send")
    public Result send(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        if (username == null || username.trim().isEmpty()) {
            return Result.error("请输入账号");
        }
        String code = RandomUtil.randomNumbers(6);
        redisTemplate.opsForValue().set("captcha:" + username, code, 5, TimeUnit.MINUTES);
        // 模拟发送验证码（生产环境改为真实短信/邮件）
        System.out.println("[验证码] 用户 " + username + " 的验证码: " + code);
        return Result.success(code);
    }
}
