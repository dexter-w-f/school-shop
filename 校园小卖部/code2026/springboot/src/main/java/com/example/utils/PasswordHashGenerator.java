package com.example.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具 - 用于生成默认密码的哈希值
 * 运行 main 方法生成 SQL 更新语句
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 "123456" 的哈希值（用户默认密码）
        String userPassword = encoder.encode("123456");
        System.out.println("========== 用户默认密码哈希值 ==========");
        System.out.println("明文密码: 123456");
        System.out.println("哈希值: " + userPassword);
        System.out.println("\nSQL 语句:");
        System.out.println("UPDATE user SET password = '" + userPassword + "';\n");
        
        // 生成 "admin" 的哈希值（管理员默认密码）
        String adminPassword = encoder.encode("admin");
        System.out.println("========== 管理员默认密码哈希值 ==========");
        System.out.println("明文密码: admin");
        System.out.println("哈希值: " + adminPassword);
        System.out.println("\nSQL 语句:");
        System.out.println("UPDATE admin SET password = '" + adminPassword + "';\n");
        
        System.out.println("========== 完整 SQL 脚本 ==========");
        System.out.println("-- 执行以下 SQL 语句更新所有用户的密码为加密格式");
        System.out.println("UPDATE user SET password = '" + userPassword + "';");
        System.out.println("UPDATE admin SET password = '" + adminPassword + "';");
        System.out.println("\n-- 注意：这会将所有现有用户的密码重置为默认密码");
        System.out.println("-- 建议在执行前备份数据库！");
    }
}
