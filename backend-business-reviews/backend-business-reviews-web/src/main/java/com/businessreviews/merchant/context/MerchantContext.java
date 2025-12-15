package com.businessreviews.merchant.context;

import com.businessreviews.exception.BusinessException;

/**
 * 商家用户上下文，用于在请求中存储当前商家用户信息
 */
public class MerchantContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> MERCHANT_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Long requireUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return userId;
    }

    public static void setMerchantId(Long merchantId) {
        MERCHANT_ID.set(merchantId);
    }

    public static Long getMerchantId() {
        return MERCHANT_ID.get();
    }

    public static Long requireMerchantId() {
        Long merchantId = MERCHANT_ID.get();
        if (merchantId == null) {
            throw new BusinessException(401, "未登录");
        }
        return merchantId;
    }

    public static void clear() {
        USER_ID.remove();
        MERCHANT_ID.remove();
    }
}
