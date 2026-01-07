package com.businessreviews.enums;

import lombok.Getter;

/**
 * 笔记状态枚举
 * 
 * 状态流转说明：
 * 1. 用户发布笔记 -> PENDING（审核中）
 * 2. AI审核通过 -> NORMAL（正常/已发布）
 * 3. AI审核不通过 -> HIDDEN（隐藏/已拒绝）或 REJECTED（违规）
 * 4. 用户手动隐藏 -> HIDDEN
 * 
 * @author businessreviews
 */
@Getter
public enum NoteStatus {
    /** 已删除 - 用户删除或系统删除 */
    DELETED(0, "已删除"),
    /** 正常/已发布 - 审核通过，公开可见 */
    NORMAL(1, "正常"),
    /** 隐藏/已拒绝 - 审核不通过或用户隐藏，不公开显示 */
    HIDDEN(2, "隐藏"),
    /** 审核中 - 新发布内容，等待AI审核 */
    PENDING(3, "审核中"),
    /** 违规 - 内容违反社区规范，被系统拒绝 */
    REJECTED(4, "违规");

    private final int code;
    private final String desc;

    NoteStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static NoteStatus fromCode(int code) {
        for (NoteStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
