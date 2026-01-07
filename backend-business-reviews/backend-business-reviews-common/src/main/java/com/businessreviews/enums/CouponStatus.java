package com.businessreviews.enums;

import lombok.Getter;

/**
 * 优惠券状态枚举
 * <p>
 * 用于标识优惠券的状态
 * </p>
 * 
 * @author businessreviews
 */
@Getter
public enum CouponStatus {
    /** 禁用 */
    DISABLED(0, "禁用"),
    /** 启用 (默认) */
    ENABLED(1, "启用");

    private final int code;
    private final String desc;

    CouponStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static CouponStatus fromCode(int code) {
        for (CouponStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
