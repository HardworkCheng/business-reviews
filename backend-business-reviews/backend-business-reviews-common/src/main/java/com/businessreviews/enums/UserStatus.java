package com.businessreviews.enums;

import lombok.Getter;

/**
 * 用户账号状态枚举
 * <p>
 * 用于标识用户账号当前的可用状态
 * </p>
 */
@Getter
public enum UserStatus {
    /** 正常状态 (默认) */
    NORMAL(1, "正常"),
    /** 禁用状态 (无法登录) */
    DISABLED(2, "禁用");

    /** 状态码 */
    private final int code;
    /** 状态描述 */
    private final String desc;

    UserStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
