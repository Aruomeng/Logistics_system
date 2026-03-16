package com.logistics.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类 - 获取当前登录用户
 */
public class SecurityUtil {

    private SecurityUtil() {
    }

    /** 获取当前登录用户信息 */
    public static LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    /** 获取当前用户ID */
    public static Long getCurrentUserId() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /** 获取当前用户角色 */
    public static Integer getCurrentRoleType() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getRoleType() : null;
    }
}
