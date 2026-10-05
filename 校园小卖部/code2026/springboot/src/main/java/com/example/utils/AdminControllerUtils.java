package com.example.utils;

import com.example.mapper.AdminMapper;
import com.example.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;

public class AdminControllerUtils {

    private AdminControllerUtils() {
    }

    public static Integer requireAdmin(HttpServletRequest request) {
        String currentUserId = request.getHeader("X-Current-UserId");
        String currentRole = normalizeRole(request.getHeader("X-Current-Role"));
        String token = request.getHeader("token");

        // 未登录：401，前端据此跳登录页
        if (!StringUtils.hasText(currentUserId) || !StringUtils.hasText(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录，请先登录");
        }
        // 已登录但不是管理员：403。必须与 401 区分，否则前端会把"无权限"当成"登录过期"而反复跳登录页
        if (!"管理员".equals(currentRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }
        Integer userId;
        try {
            userId = Integer.valueOf(currentUserId.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录，请先登录");
        }
        String userInfo = TokenUtils.getUserInfo(token);
        if (userInfo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        if (!("user=" + userId + ";role=管理员").equals(userInfo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }
        return userId;
    }

    /**
     * 非抛异常的“是否管理员”判断，用于需要在同一接口内区分用户/管理员行为的场景。
     * 角色取自服务端 token，不信任客户端请求头。
     * @return 管理员 userId；不是管理员时返回 null
     */
    public static Integer getAdminUserId(HttpServletRequest request) {
        String currentUserId = request.getHeader("X-Current-UserId");
        String currentRole = normalizeRole(request.getHeader("X-Current-Role"));
        String token = request.getHeader("token");
        if (!StringUtils.hasText(currentUserId) || !"管理员".equals(currentRole) || !StringUtils.hasText(token)) {
            return null;
        }
        Integer userId;
        try {
            userId = Integer.valueOf(currentUserId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        String userInfo = TokenUtils.getUserInfo(token);
        if (userInfo == null || !("user=" + userId + ";role=管理员").equals(userInfo)) {
            return null;
        }
        return userId;
    }

    /**
     * 便捷布尔判断。
     */
    public static boolean isAdmin(HttpServletRequest request) {
        return getAdminUserId(request) != null;
    }

    /**
     * 兼容浏览器直接发送 UTF-8 中文角色，以及只支持 ASCII 的客户端做百分号编码的情况。
     */
    private static String normalizeRole(String role) {
        if (role == null || role.indexOf('%') < 0) {
            return role;
        }
        try {
            return java.net.URLDecoder.decode(role, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return role;
        }
    }
}
