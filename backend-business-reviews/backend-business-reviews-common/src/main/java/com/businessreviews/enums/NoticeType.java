package com.businessreviews.enums;

import lombok.Getter;

/**
 * 系统通知类型枚举
 * <p>
 * 用于消息中心的消息分类展示
 * </p>
 */
@Getter
public enum NoticeType {
    /** 点赞笔记 */
    LIKE_NOTE(1, "点赞笔记"),
    /** 评论笔记 */
    COMMENT_NOTE(2, "评论笔记"),
    /** 关注 */
    FOLLOW(3, "关注"),
    /** 点赞评论 */
    LIKE_COMMENT(4, "点赞评论");

    private final int code;
    private final String desc;

    NoticeType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
