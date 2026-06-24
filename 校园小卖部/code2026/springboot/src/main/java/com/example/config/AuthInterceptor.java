 package com.example.config;
 
 import com.example.utils.TokenUtils;
 import jakarta.servlet.http.HttpServletRequest;
 import jakarta.servlet.http.HttpServletResponse;
 import org.springframework.stereotype.Component;
 import org.springframework.web.servlet.HandlerInterceptor;
 
 @Component
 public class AuthInterceptor implements HandlerInterceptor {
 
    private static final String[] WHITELIST = {
        "/login",
        "/register",
        "/logout",
        "/files/download/",
       "/goods/selectAll",
        "/comment/selectPage",
        "/comment/selectAll",
        "/doc.html",
        "/swagger-ui",
        "/v3/api-docs",
        "/carousel/selectAll",
         "/category/selectAll"
    };
 
     @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 健康检查根路径精确匹配
        if ("/".equals(path) || path.isEmpty()) {
            return true;
        }

        for (String whitePath : WHITELIST) {
            if (path.startsWith(whitePath)) {
                return true;
            }
        }
 
         String token = request.getHeader("token");
         if (TokenUtils.validateToken(token)) {
             return true;
         }
 
         response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"401\",\"msg\":\"未登录，请先登录\"}");
        response.getWriter().flush();
        return false;
     }
 }
