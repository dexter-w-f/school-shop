 package com.example.utils;
 
 import org.springframework.stereotype.Component;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * 登录失败限流。
  *
  * 修复要点：原先读取路径（isLocked）在锁定期满时会直接删除计数，
  * 导致只要再请求一次就能把计数清零、锁定形同虚设。现在读取路径不再修改数据，
  * 计数只由 recordFailure / reset 变更，锁定期满后在下次记录失败时自然重置。
  */
 @Component
 public class LoginAttemptLimiter {
 
     private static final int MAX_ATTEMPTS = 5;
     private static final long LOCK_DURATION_MS = 15 * 60 * 1000; // 15分钟
 
     private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
 
     /**
      * 检查用户名是否被锁定（只读，不修改计数）。
      */
     public boolean isLocked(String username) {
         if (username == null) return false;
         AttemptInfo info = attempts.get(username);
         return isLocked(info);
     }
 
     private boolean isLocked(AttemptInfo info) {
         if (info == null || info.attempts < MAX_ATTEMPTS) return false;
         // attempts / lockTime 在 compute 内可能被并发修改，这里做一次快照读取
         int currentAttempts = info.attempts;
         long currentLockTime = info.lockTime;
         if (currentAttempts < MAX_ATTEMPTS) return false;
         return System.currentTimeMillis() - currentLockTime <= LOCK_DURATION_MS;
     }
 
     /**
      * 记录一次登录失败
      */
     public void recordFailure(String username) {
         if (username == null) return;
         attempts.compute(username, (key, info) -> {
             long now = System.currentTimeMillis();
             // 首次失败，或上一轮锁定期已过 → 重新计数
             if (info == null || now - info.lockTime > LOCK_DURATION_MS) {
                 return new AttemptInfo(1, now);
             }
             info.attempts++;
             info.lockTime = now;
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
         volatile int attempts;
         volatile long lockTime;
 
         AttemptInfo(int attempts, long lockTime) {
             this.attempts = attempts;
             this.lockTime = lockTime;
         }
     }
 }
