package com.businessreviews.enums;

import lombok.Getter;

/**
 * 笔记状态枚举
 */
@Getter
public enum NoteStatus {
    /** 正常 */
    NORMAL(1, "正常"),
    /** 隐藏 */
    HIDDEN(2, "隐藏"),
    /** 审核中 */
    PENDING(3, "审核中");

    private final int code;
    private final String desc;

    NoteStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
