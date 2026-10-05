package com.example.config;

import com.example.utils.AdminControllerUtils;
import jakarta.servlet.http.HttpServletRequest;

public class AuthValidator {

    private AuthValidator() {
    }

    public static Integer requireUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("currentUserId");
        if (userIdObj == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
        }
        return (Integer) userIdObj;
    }

    /**
     * 允许「管理员」或「资源所有者本人」操作，否则抛 403。
     * 用于普通用户可参与的写操作（发帖、回帖、评论、收藏等）。
     */
    public static void requireAdminOrOwner(HttpServletRequest request, Integer ownerUserId) {
        if (AdminControllerUtils.isAdmin(request)) {
            return;
        }
        Integer currentUserId = requireUserId(request);
        if (ownerUserId == null || !currentUserId.equals(ownerUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "无权操作该数据");
        }
    }

    /**
     * 同上，但以「查询到的当前用户ID」作为所有者判断依据。
     */
    public static void requireAdminOrSelf(HttpServletRequest request, Integer selfUserId) {
        if (AdminControllerUtils.isAdmin(request)) {
            return;
        }
        Integer currentUserId = requireUserId(request);
        if (selfUserId == null || !currentUserId.equals(selfUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "无权操作该数据");
        }
    }
}
