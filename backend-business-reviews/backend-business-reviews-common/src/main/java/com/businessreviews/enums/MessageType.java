package com.businessreviews.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
public enum MessageType {
    /** 文本 */
    TEXT(1, "文本"),
    /** 图片 */
    IMAGE(2, "图片"),
    /** 语音 */
    VOICE(3, "语音");

    private final int code;
    private final String desc;

    MessageType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
