package com.businessreviews.enums;

import lombok.Getter;

/**
 * 商家营业状态枚举
 * <p>
 * 决定商家在前端的展示状态及是否可接单
 * </p>
 */
@Getter
public enum ShopStatus {
    /** 营业中 */
    OPEN(1, "营业中"),
    /** 休息中 */
    REST(2, "休息中"),
    /** 已关闭 */
    CLOSED(3, "已关闭");

    private final int code;
    private final String desc;

    ShopStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
