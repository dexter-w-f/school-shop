package com.example.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的Token管理工具
 *
 * 同时保存 token -> 登录用户信息，避免仅凭 token 就能跨账号伪造请求。
 *
 * 本轮补充：
 * - token 增加**有效期**（默认 2 小时），过期后校验不通过并自动清理，
 *   不再出现"token 泄露即永久有效"；
 * - 三个映射表同步清理，避免过期条目无限堆积导致内存泄漏。
 *
 * 仍属单机内存实现：重启会失效、多实例不共享，如需跨实例请换 JWT 或 Redis 存储。
 */
public class TokenUtils {

    /** token 有效期（毫秒），默认 2 小时 */
    private static final long TOKEN_TTL_MS = 2 * 60 * 60 * 1000L;

    private static final Map<String, String> TOKEN_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> USER_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> TOKEN_USER_INFO_MAP = new ConcurrentHashMap<>();
    /** token -> 过期时间戳 */
    private static final Map<String, Long> TOKEN_EXPIRE_MAP = new ConcurrentHashMap<>();

    public static String generateToken(Integer userId) {
        return generateToken(userId, null);
    }

    public static String generateToken(Integer userId, String role) {
        String token = UUID.randomUUID().toString();
        String key = String.valueOf(userId);
        String oldToken = TOKEN_MAP.get(key);
        if (oldToken != null) {
            // 同一用户重新登录，作废旧 token
            USER_MAP.remove(oldToken);
            TOKEN_USER_INFO_MAP.remove(oldToken);
            TOKEN_EXPIRE_MAP.remove(oldToken);
        }
        TOKEN_MAP.put(key, token);
        USER_MAP.put(token, key);
        TOKEN_USER_INFO_MAP.put(token, role == null
                ? "user=" + userId
                : "user=" + userId + ";role=" + emptyToNull(role));
        TOKEN_EXPIRE_MAP.put(token, System.currentTimeMillis() + TOKEN_TTL_MS);
        return token;
    }

    /**
     * 校验 token：必须存在且未过期。过期时顺手清理相关条目。
     */
    public static boolean validateToken(String token) {
        if (token == null || !USER_MAP.containsKey(token)) {
            return false;
        }
        Long expireAt = TOKEN_EXPIRE_MAP.get(token);
        if (expireAt == null) {
            // 历史遗留（无过期信息）视为有效，首次校验时补上有效期
            TOKEN_EXPIRE_MAP.put(token, System.currentTimeMillis() + TOKEN_TTL_MS);
            return true;
        }
        if (System.currentTimeMillis() > expireAt) {
            removeToken(token);
            return false;
        }
        return true;
    }

    public static String getUserInfo(String token) {
        if (token == null || !validateToken(token)) {
            return null;
        }
        return TOKEN_USER_INFO_MAP.get(token);
    }

    public static void removeToken(String token) {
        if (token == null) {
            return;
        }
        String userId = USER_MAP.remove(token);
        if (userId != null) {
            TOKEN_MAP.remove(userId);
        }
        TOKEN_USER_INFO_MAP.remove(token);
        TOKEN_EXPIRE_MAP.remove(token);
    }

    public static void removeByUserId(Integer userId) {
        if (userId == null) {
            return;
        }
        String token = TOKEN_MAP.remove(String.valueOf(userId));
        if (token != null) {
            USER_MAP.remove(token);
            TOKEN_USER_INFO_MAP.remove(token);
            TOKEN_EXPIRE_MAP.remove(token);
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
