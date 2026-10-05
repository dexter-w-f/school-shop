package com.example.config;

import com.example.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;
 
 @Component
 public class AuthInterceptor implements HandlerInterceptor {
 
    private static final String[] WHITELIST = {
        "/login",
        "/register",
        "/logout",
        "/goods/selectAll",
        "/carousel/selectAll",
        "/category/selectAll",
        "/captcha/",
        "/doc.html",
        "/swagger-ui",
        "/v3/api-docs"
    };
 
    @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (isWhitelistPath(path)) {
            return true;
        }
 
        String token = request.getHeader("token");
        String currentUserId = request.getHeader("X-Current-UserId");
        String currentRole = request.getHeader("X-Current-Role");
        if (token != null && TokenUtils.validateToken(token)) {
            String userInfo = TokenUtils.getUserInfo(token);
            boolean tokenBindOk = isTokenBindOk(userInfo, currentUserId, currentRole);
            if (tokenBindOk) {
                bindCurrentUser(request, currentUserId, currentRole);
                return true;
            }

            TokenUtils.removeByUserId(parseUserId(userInfo));
        }
 
         response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"401\",\"msg\":\"未登录，请先登录\"}");
        response.getWriter().flush();
        return false;
    }

    private boolean isWhitelistPath(String path) {
        if ("/".equals(path) || path.isEmpty()) {
            return true;
        }
        for (String whitePath : WHITELIST) {
            if (path.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }

    private void bindCurrentUser(HttpServletRequest request, String currentUserId, String currentRole) {
        try {
            if (currentUserId != null) {
                request.setAttribute("currentUserId", Integer.valueOf(currentUserId.trim()));
            }
            request.setAttribute("currentRole", currentRole);
            request.setAttribute("currentToken", request.getHeader("token"));
        } catch (NumberFormatException ignored) {
            request.removeAttribute("currentUserId");
        }
    }

    private boolean isTokenBindOk(String userInfo, String currentUserId, String currentRole) {
        if (userInfo == null) {
            return false;
        }
        if (!String.valueOf(parseUserId(userInfo)).equals(emptyToNull(currentUserId))) {
            return false;
        }
        if (currentRole == null || currentRole.isBlank()) {
            return false;
        }
        return userInfo.contains("role=" + normalizeRole(currentRole));
    }

    /**
     * 兼容两种客户端：
     * - 浏览器/axios 直接发送 UTF-8 中文角色
     * - 部分 HTTP 客户端只能发送 ASCII，会把中文角色做百分号编码（如 %E7%AE%A1%E7%90%86%E5%91%98）
     * 这里统一解码成原始角色再比对。
     */
    private String normalizeRole(String role) {
        if (role == null || role.indexOf('%') < 0) {
            return role;
        }
        try {
            return java.net.URLDecoder.decode(role, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return role;
        }
    }

    private Integer parseUserId(String userInfo) {
        if (userInfo == null) {
            return null;
        }
        int index = userInfo.indexOf("user=");
        if (index < 0) {
            return null;
        }
        String value = userInfo.substring(index + 5);
        int end = value.indexOf(';');
        if (end >= 0) {
            value = value.substring(0, end);
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
 }
