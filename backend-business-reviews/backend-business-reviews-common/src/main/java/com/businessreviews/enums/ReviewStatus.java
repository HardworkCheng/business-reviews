package com.businessreviews.enums;

import lombok.Getter;

/**
 * 评价（商家点评）状态枚举
 * <p>
 * 用于标识用户对商家店铺的评价状态
 * </p>
 * 
 * @author businessreviews
 */
@Getter
public enum ReviewStatus {
    /** 正常显示 */
    NORMAL(1, "正常"),
    /** 隐藏/删除 */
    HIDDEN(0, "隐藏");

    private final int code;
    private final String desc;

    ReviewStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static ReviewStatus fromCode(int code) {
        for (ReviewStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
