package com.businessreviews.enums;

import lombok.Getter;

/**
 * 验证码类型枚举
 */
@Getter
public enum CodeType {
    /** 登录 */
    LOGIN(1, "登录"),
    /** 注册 */
    REGISTER(2, "注册"),
    /** 重置密码 */
    RESET_PASSWORD(3, "重置密码"),
    /** 修改手机号-验证原手机 */
    CHANGE_PHONE_OLD(4, "修改手机号-验证原手机"),
    /** 修改手机号-验证新手机 */
    CHANGE_PHONE_NEW(5, "修改手机号-验证新手机");

    private final int code;
    private final String desc;

    CodeType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
