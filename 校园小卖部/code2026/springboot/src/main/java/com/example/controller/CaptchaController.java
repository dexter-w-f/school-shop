package com.example.controller;

import cn.hutool.core.util.RandomUtil;
import com.example.common.Result;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    /** 同一邮箱在窗口内的发送次数上限 */
    private static final long SEND_LIMIT = 3;
    /** 发送次数统计窗口（分钟） */
    private static final long LIMIT_WINDOW_MINUTES = 1;
    /** 验证码有效期（分钟） */
    private static final long CODE_TTL_MINUTES = 5;
    /** 邮件发送进程最长等待时间（秒），避免子进程卡死占满线程 */
    private static final long SEND_TIMEOUT_SECONDS = 15;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /** 邮件脚本路径，可通过 captcha.script-path 配置覆盖 */
    @Value("${captcha.script-path:}")
    private String scriptPath;

    @Value("${captcha.node-path:node}")
    private String nodePath;

    @PostMapping("/send")
    public Result send(@RequestBody Map<String, String> params) {
        String email = params.get("username");
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@.+$")) {
            return Result.error("邮箱格式不正确");
        }

        // 频率限制：统计“发送尝试”次数，避免只统计成功次数导致可被反复触发
        String limitKey = "captcha:send_limit:" + email;
        Long count;
        try {
            count = redisTemplate.opsForValue().increment(limitKey);
            if (count != null && count == 1) {
                redisTemplate.expire(limitKey, LIMIT_WINDOW_MINUTES, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            // Redis 不可用时给出明确提示，而不是抛 500
            System.err.println("[Captcha] Redis 不可用: " + e.getMessage());
            return Result.error("验证码服务暂时不可用，请稍后重试");
        }
        if (count != null && count > SEND_LIMIT) {
            return Result.error("发送过于频繁，请稍后再试");
        }

        String code = RandomUtil.randomNumbers(6);
        try {
            redisTemplate.opsForValue().set("captcha:" + email, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("[Captcha] 验证码写入失败: " + e.getMessage());
            return Result.error("验证码服务暂时不可用，请稍后重试");
        }

        boolean sent = sendMail(email, code);
        if (!sent) {
            // 发送失败时移除验证码，避免占用一次有效验证码
            try {
                redisTemplate.delete("captcha:" + email);
            } catch (Exception ignored) {
            }
            return Result.error("验证码发送失败，请稍后重试");
        }
        // 注意：绝不能把验证码返回给调用方，否则邮箱验证形同虚设
        return Result.success("验证码已发送，请查收邮件");
    }

    /**
     * 调用外部 node 脚本发送邮件。
     *
     * @return 是否发送成功
     */
    private boolean sendMail(String email, String code) {
        String path = resolveScriptPath();
        if (path == null) {
            System.err.println("[Captcha] 未找到邮件脚本，请配置 captcha.script-path");
            return false;
        }
        try {
            Process p = new ProcessBuilder(nodePath, path, email, code).start();
            java.io.BufferedReader errReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder errMsg = new StringBuilder();
            String line;
            while ((line = errReader.readLine()) != null) {
                errMsg.append(line);
            }
            if (!p.waitFor(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                System.err.println("[Captcha] 邮件脚本超时: " + path);
                return false;
            }
            if (p.exitValue() == 0) {
                return true;
            }
            System.err.println("[Captcha] 邮件发送失败: " + errMsg);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            System.err.println("[Captcha] 邮件发送异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 解析邮件脚本路径；未配置时回退到项目内约定位置。
     */
    private String resolveScriptPath() {
        if (scriptPath != null && !scriptPath.isBlank()) {
            return scriptPath;
        }
        String fallback = System.getProperty("user.dir") + "/../vue/email-sender.js";
        java.io.File f = new java.io.File(fallback);
        return f.exists() ? f.getAbsolutePath() : null;
    }
}
