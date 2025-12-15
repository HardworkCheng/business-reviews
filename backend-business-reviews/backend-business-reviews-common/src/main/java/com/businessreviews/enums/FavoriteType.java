package com.businessreviews.enums;

import lombok.Getter;

/**
 * 收藏类型枚举
 */
@Getter
public enum FavoriteType {
    /** 笔记 */
    NOTE(1, "笔记"),
    /** 商家 */
    SHOP(2, "商家");

    private final int code;
    private final String desc;

    FavoriteType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
