package com.example.controller;

import cn.hutool.core.util.RandomUtil;
import com.example.common.Result;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/send")
    public Result send(@RequestBody Map<String, String> params) {
        String email = params.get("username");
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@.+$")) {
            return Result.error("请输入正确的邮箱");
        }
        String code = RandomUtil.randomNumbers(6);
        redisTemplate.opsForValue().set("captcha:" + email, code, 5, TimeUnit.MINUTES);

        try {
            String scriptPath = new File(System.getProperty("user.dir")).getParent() + "/vue/email-sender.js";
            Process p = new ProcessBuilder("node", scriptPath, email, code).start();
            if (p.waitFor() == 0) return Result.success();
            return Result.error("邮件发送失败，请稍后重试");
        } catch (Exception e) {
            return Result.error("邮件服务异常，请稍后重试");
        }
    }
}
