 package com.example.utils;
 
 import org.springframework.stereotype.Component;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 @Component
 public class LoginAttemptLimiter {
 
     private static final int MAX_ATTEMPTS = 5;
     private static final long LOCK_DURATION_MS = 15 * 60 * 1000; // 15分钟
 
     private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
 
     /**
      * 检查用户名是否被锁定
      */
     public boolean isLocked(String username) {
         if (username == null) return false;
         AttemptInfo info = attempts.get(username);
         if (info == null) return false;
         if (info.attempts >= MAX_ATTEMPTS) {
             if (System.currentTimeMillis() - info.lockTime > LOCK_DURATION_MS) {
                 attempts.remove(username);
                 return false;
             }
             return true;
         }
         return false;
     }
 
     /**
      * 记录一次登录失败
      */
     public void recordFailure(String username) {
         if (username == null) return;
         attempts.compute(username, (key, info) -> {
             if (info == null || System.currentTimeMillis() - info.lockTime > LOCK_DURATION_MS) {
                 return new AttemptInfo(1, System.currentTimeMillis());
             }
             info.attempts++;
             info.lockTime = System.currentTimeMillis();
             return info;
         });
     }
 
     /**
      * 登录成功后重置
      */
     public void reset(String username) {
         if (username == null) return;
         attempts.remove(username);
     }
 
     /**
      * 获取剩余锁定时间（毫秒）
      */
     public long getRemainingLockTime(String username) {
         if (username == null) return 0;
         AttemptInfo info = attempts.get(username);
         if (info == null || info.attempts < MAX_ATTEMPTS) return 0;
         long elapsed = System.currentTimeMillis() - info.lockTime;
         return Math.max(0, LOCK_DURATION_MS - elapsed);
     }
 
     private static class AttemptInfo {
         int attempts;
         long lockTime;
 
         AttemptInfo(int attempts, long lockTime) {
             this.attempts = attempts;
             this.lockTime = lockTime;
         }
     }
 }
