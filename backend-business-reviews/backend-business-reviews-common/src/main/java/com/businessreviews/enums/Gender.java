package com.businessreviews.enums;

import lombok.Getter;

/**
 * 用户性别枚举
 * <p>
 * ISO/IEC 5218 国际标准建议：0=未知, 1=男, 2=女, 9=不适用
 * 本系统采用简化标准
 * </p>
 */
@Getter
public enum Gender {
    /** 未知/保密 */
    UNKNOWN(0, "未知"),
    /** 男性 */
    MALE(1, "男"),
    /** 女性 */
    FEMALE(2, "女");

    private final int code;
    private final String desc;

    Gender(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
