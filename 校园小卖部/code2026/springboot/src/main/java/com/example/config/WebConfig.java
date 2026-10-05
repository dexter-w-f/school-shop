 package com.example.config;
 
 import jakarta.annotation.Resource;
 import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
 @Configuration
 public class WebConfig implements WebMvcConfigurer {
 
     @Resource
     private AuthInterceptor authInterceptor;
 
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/logout",
                        "/goods/selectAll",
                        "/carousel/selectAll",
                        "/category/selectAll",
                        "/captcha/**",
                        "/doc.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/files/**",
                        "/",
                        "/error"
                );
    }

    /**
     * 把上传目录映射为静态资源。
     * 浏览器的 img 标签无法携带自定义 token 头，若只走 /files/download 控制器鉴权，
     * 所有上传图片对普通用户都会 403。
     */
    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        String base = com.example.utils.FileStorage.baseDir();
        String location = java.nio.file.Paths.get(base).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/files/download/**")
                .addResourceLocations(location);
    }
}
