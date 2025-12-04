package com.businessreviews.context;

/**
 * 用户上下文，用于在请求中存储当前用户信息
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
