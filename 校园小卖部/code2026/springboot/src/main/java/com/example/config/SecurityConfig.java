package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security配置类 - 仅使用BCrypt加密功能，禁用安全拦截
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * 配置安全过滤链，放行所有请求
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(CsrfConfigurer::disable)  // 禁用CSRF
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // 允许所有请求
            );
        return http.build();
    }
}
