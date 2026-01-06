package com.businessreviews.enums;

import lombok.Getter;

/**
 * 验证码业务类型枚举
 * <p>
 * 区分不同业务场景下的验证码，防止验证码跨场景混用
 * </p>
 */
@Getter
public enum CodeType {
    /** 登录验证码 */
    LOGIN(1, "登录"),
    /** 注册验证码 */
    REGISTER(2, "注册"),
    /** 重置密码验证码 */
    RESET_PASSWORD(3, "重置密码"),
    /** 修改手机号-验证旧手机 */
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
