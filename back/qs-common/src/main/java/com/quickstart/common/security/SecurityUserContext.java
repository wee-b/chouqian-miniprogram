package com.quickstart.common.security;

import com.quickstart.common.domain.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUserContext {

    /**
     * 存储当前线程的 token（ThreadLocal 安全）
     */
    private static final ThreadLocal<String> TOKEN_HOLDER = new ThreadLocal<>();

    private SecurityUserContext() {
    }

    // ====================== 原有方法保持不变 ======================
    public static String getCurrentMemberCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser.getUsername();
        }
        return String.valueOf(principal);
    }

    public static LoginUser getCurrentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
    }

    // ====================== 新增：Token 操作方法 ======================

    /**
     * 设置当前请求的 token
     */
    public static void setToken(String token) {
        TOKEN_HOLDER.set(token);
    }

    /**
     * 获取当前请求的 token
     */
    public static String getToken() {
        return TOKEN_HOLDER.get();
    }

    /**
     * 清空上下文信息（登出时调用）
     */
    public static void clear() {
        TOKEN_HOLDER.remove();
        SecurityContextHolder.clearContext();
    }
}