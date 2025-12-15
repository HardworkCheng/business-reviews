package com.businessreviews.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {
    /** 正常 */
    NORMAL(1, "正常"),
    /** 禁用 */
    DISABLED(2, "禁用");

    private final int code;
    private final String desc;

    UserStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
