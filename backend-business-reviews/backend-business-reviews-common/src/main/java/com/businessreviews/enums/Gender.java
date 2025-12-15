package com.businessreviews.enums;

import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
public enum Gender {
    /** 未知 */
    UNKNOWN(0, "未知"),
    /** 男 */
    MALE(1, "男"),
    /** 女 */
    FEMALE(2, "女");

    private final int code;
    private final String desc;

    Gender(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
