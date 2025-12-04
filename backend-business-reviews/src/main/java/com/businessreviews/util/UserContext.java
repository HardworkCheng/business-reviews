package com.businessreviews.util;

/**
 * 用户上下文（存储当前登录用户信息）
 */
public class UserContext {
    
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    
    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return userIdHolder.get();
    }
    
    /**
     * 清除当前用户ID
     */
    public static void clear() {
        userIdHolder.remove();
    }
    
    /**
     * 判断是否已登录
     */
    public static boolean isLogin() {
        return userIdHolder.get() != null;
    }
}
