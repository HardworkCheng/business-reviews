package com.businessreviews.enums;

import lombok.Getter;

/**
 * 笔记类型枚举
 * <p>
 * 区分用户笔记和商家笔记
 * </p>
 * 
 * @author businessreviews
 */
@Getter
public enum NoteType {
    /** 用户笔记 - 普通用户发布的笔记 */
    USER(1, "用户笔记"),
    /** 商家笔记 - 商家账号发布的推广笔记 */
    MERCHANT(2, "商家笔记");

    private final int code;
    private final String desc;

    NoteType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static NoteType fromCode(int code) {
        for (NoteType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
}
