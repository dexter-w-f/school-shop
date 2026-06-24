 package com.example.utils;
 
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * 简单的Token管理工具
  */
 public class TokenUtils {
 
     private static final Map<String, String> TOKEN_MAP = new ConcurrentHashMap<>();
     private static final Map<String, String> USER_MAP = new ConcurrentHashMap<>();
 
     public static String generateToken(Integer userId) {
         String token = UUID.randomUUID().toString();
         String key = String.valueOf(userId);
         String oldToken = TOKEN_MAP.get(key);
         if (oldToken != null) {
             USER_MAP.remove(oldToken);
         }
         TOKEN_MAP.put(key, token);
         USER_MAP.put(token, key);
         return token;
     }
 
     public static boolean validateToken(String token) {
         return token != null && USER_MAP.containsKey(token);
     }
 
     public static void removeToken(String token) {
         String userId = USER_MAP.remove(token);
         if (userId != null) {
             TOKEN_MAP.remove(userId);
         }
     }
 
     public static void removeByUserId(Integer userId) {
         String key = String.valueOf(userId);
         String token = TOKEN_MAP.remove(key);
         if (token != null) {
             USER_MAP.remove(token);
         }
     }
 }
