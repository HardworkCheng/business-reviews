package com.businessreviews.enums;

import lombok.Getter;

/**
 * 话题状态枚举
 * <p>
 * 用于标识话题的状态
 * </p>
 * 
 * @author businessreviews
 */
@Getter
public enum TopicStatus {
    /** 正常 */
    NORMAL(1, "正常"),
    /** 禁用 */
    DISABLED(0, "禁用");

    private final int code;
    private final String desc;

    TopicStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static TopicStatus fromCode(int code) {
        for (TopicStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
