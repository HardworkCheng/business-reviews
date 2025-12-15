package com.businessreviews.enums;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {
    /** 手机号格式错误 */
    PHONE_FORMAT_ERROR(40001, "手机号格式错误"),
    /** 验证码错误或已过期 */
    CODE_ERROR(40002, "验证码错误或已过期"),
    /** 用户不存在 */
    USER_NOT_FOUND(40003, "用户不存在"),
    /** 参数缺失 */
    PARAM_MISSING(40004, "参数缺失"),
    /** 操作过于频繁 */
    TOO_FREQUENT(40005, "操作过于频繁"),
    /** 未登录 */
    UNAUTHORIZED(40101, "未登录"),
    /** Token已过期 */
    TOKEN_EXPIRED(40102, "Token已过期"),
    /** Token无效 */
    TOKEN_INVALID(40103, "Token无效"),
    /** 无权限操作 */
    FORBIDDEN(40301, "无权限操作"),
    /** 资源不存在 */
    NOT_FOUND(40401, "资源不存在"),
    /** 笔记不存在 */
    NOTE_NOT_FOUND(40402, "笔记不存在"),
    /** 商家不存在 */
    SHOP_NOT_FOUND(40403, "商家不存在"),
    /** 服务器内部错误 */
    SERVER_ERROR(50001, "服务器内部错误"),
    /** 数据库错误 */
    DB_ERROR(50002, "数据库错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
