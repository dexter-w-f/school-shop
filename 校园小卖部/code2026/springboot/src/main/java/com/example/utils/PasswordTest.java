package com.example.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 简单的密码测试工具
 */
public class PasswordTest {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 "admin" 的哈希值
        String adminPassword = encoder.encode("admin");
        System.out.println("========== 管理员密码 ==========");
        System.out.println("明文: admin");
        System.out.println("加密后: " + adminPassword);
        System.out.println();
        
        // 验证
        boolean matches = encoder.matches("admin", adminPassword);
        System.out.println("验证结果: " + (matches ? "成功" : "失败"));
        System.out.println();
        
        // 生成 "123456" 的哈希值
        String userPassword = encoder.encode("123456");
        System.out.println("========== 用户密码 ==========");
        System.out.println("明文: 123456");
        System.out.println("加密后: " + userPassword);
        System.out.println();
        
        // 验证
        boolean userMatches = encoder.matches("123456", userPassword);
        System.out.println("验证结果: " + (userMatches ? "成功" : "失败"));
        System.out.println();
        
        System.out.println("========== SQL 语句 ==========");
        System.out.println("-- 复制以下 SQL 到 MySQL 执行：");
        System.out.println("UPDATE admin SET password = '" + adminPassword + "' WHERE username = 'admin';");
        System.out.println("UPDATE user SET password = '" + userPassword + "';");
    }
}
