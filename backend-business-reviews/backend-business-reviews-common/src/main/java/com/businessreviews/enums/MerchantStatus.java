package com.businessreviews.enums;

import lombok.Getter;

/**
 * 商家状态枚举
 * <p>
 * 用于标识商家账号的状态
 * </p>
 * 
 * @author businessreviews
 */
@Getter
public enum MerchantStatus {
    /** 正常 */
    NORMAL(1, "正常"),
    /** 禁用 */
    DISABLED(2, "禁用"),
    /** 待审核 */
    PENDING(3, "待审核");

    private final int code;
    private final String desc;

    MerchantStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static MerchantStatus fromCode(int code) {
        for (MerchantStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
