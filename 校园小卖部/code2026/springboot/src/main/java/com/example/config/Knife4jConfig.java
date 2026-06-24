 package com.example.config;
 
 import io.swagger.v3.oas.models.OpenAPI;
 import io.swagger.v3.oas.models.info.Info;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 @Configuration
 public class Knife4jConfig {
 
     @Bean
     public OpenAPI customOpenAPI() {
         return new OpenAPI()
                 .info(new Info()
                         .title("校园小卖部 API")
                         .version("1.0")
                         .description("校园小卖部电商管理系统后端接口文档"));
     }
 }
