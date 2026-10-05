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
        String currentRole = request.getHeader("X-Current-Role");
        String token = request.getHeader("token");
        if (!StringUtils.hasText(currentUserId) || !"管理员".equals(currentRole) || !StringUtils.hasText(token)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "未登录，请先登录");
        }
        Integer userId;
        try {
            userId = Integer.valueOf(currentUserId.trim());
        } catch (NumberFormatException e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "未登录，请先登录");
        }
        String userInfo = TokenUtils.getUserInfo(token);
        if (userInfo == null || !("user=" + userId + ";role=管理员").equals(userInfo)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录，请先登录");
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
        String currentRole = request.getHeader("X-Current-Role");
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
}
