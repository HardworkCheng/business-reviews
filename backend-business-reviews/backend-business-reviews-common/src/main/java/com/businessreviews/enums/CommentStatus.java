package com.businessreviews.enums;

import lombok.Getter;

/**
 * 评论状态枚举
 */
@Getter
public enum CommentStatus {
    /** 已删除 */
    DELETED(0, "已删除"),
    /** 正常显示 */
    NORMAL(1, "正常"),
    /** 隐藏 (如被折叠或违规屏蔽) */
    HIDDEN(2, "隐藏");

    private final int code;
    private final String desc;

    CommentStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
