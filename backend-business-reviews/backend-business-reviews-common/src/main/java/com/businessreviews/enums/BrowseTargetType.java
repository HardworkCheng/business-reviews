package com.businessreviews.enums;

import lombok.Getter;

/**
 * 浏览目标类型枚举
 * <p>
 * 用于标识用户浏览、点赞、收藏等操作的对象类型
 * </p>
 */
@Getter
public enum BrowseTargetType {
    /** 笔记 */
    NOTE(1, "笔记"),
    /** 商家 */
    SHOP(2, "商家");

    private final int code;
    private final String desc;

    BrowseTargetType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
