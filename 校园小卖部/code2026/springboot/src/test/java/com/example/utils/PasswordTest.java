package com.example.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 哈希生成/校验工具（测试域，不会进入生产 jar）。
 *
 * 用途：生成可直接写进 SQL 迁移脚本的 BCrypt 哈希，并**当场校验**，
 * 避免再次出现"脚本里的哈希根本无法通过校验"的问题。
 *
 * 用法：
 *   生成某个口令的哈希：mvn -o test-compile exec:java -Dexec.mainClass=... （或直接用 IDE 运行）
 *   默认（无参数）：生成 admin 与 123456 的哈希并校验
 */
public class PasswordTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String[] plaintexts = args.length > 0 ? args : new String[]{"admin", "123456"};
        for (String plain : plaintexts) {
            String hash = encoder.encode(plain);
            boolean ok = encoder.matches(plain, hash);
            System.out.println("plaintext = " + plain);
            System.out.println("hash      = " + hash);
            System.out.println("verify    = " + ok);
            System.out.println();
        }
        System.out.println("-- 可直接粘贴到 SQL 脚本（注意：每次生成的哈希都不同，但都能通过校验）");
    }
}
