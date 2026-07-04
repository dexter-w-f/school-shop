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
            String scriptPath = System.getProperty("user.dir") + "/校园小卖部/code2026/vue/email-sender.js";
            System.err.println("[Captcha] 脚本路径: " + scriptPath);
            Process p = new ProcessBuilder("C:\\Program Files\\nodejs\\node.exe", scriptPath, email, code).start();
            // 读取错误输出
            java.io.BufferedReader errReader = new java.io.BufferedReader(new java.io.InputStreamReader(p.getErrorStream()));
            StringBuilder errMsg = new StringBuilder();
            String line;
            while ((line = errReader.readLine()) != null) errMsg.append(line);
            int exit = p.waitFor();
            if (exit == 0) return Result.success();
            System.err.println("[Captcha] 脚本错误: " + errMsg);
            return Result.error("邮件发送失败");
        } catch (Exception e) {
            System.err.println("[Captcha] 异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("邮件服务异常，请稍后重试");
        }
    }
}

