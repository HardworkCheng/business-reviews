package com.businessreviews.enums;

import lombok.Getter;

/**
 * 用户优惠券状态枚举
 * <p>
 * 用于标识用户领取的优惠券状态
 * </p>
 * 
 * @author businessreviews
 */
@Getter
public enum UserCouponStatus {
    /** 未使用 (默认) */
    UNUSED(1, "未使用"),
    /** 已使用 */
    USED(2, "已使用"),
    /** 已过期 */
    EXPIRED(3, "已过期");

    private final int code;
    private final String desc;

    UserCouponStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static UserCouponStatus fromCode(int code) {
        for (UserCouponStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
