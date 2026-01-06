package com.businessreviews.context;

/**
 * 用户上下文管理工具
 * <p>
 * 基于ThreadLocal实现，用于在一次请求的生命周期内存储和获取当前登录用户的ID。
 * 通常在拦截器(Interceptor)中设置，在Controller或Service中获取。
 * </p>
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Long requireUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
