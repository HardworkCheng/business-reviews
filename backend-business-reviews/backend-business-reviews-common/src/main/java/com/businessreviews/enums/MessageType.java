package com.businessreviews.enums;

import lombok.Getter;

/**
 * 聊天消息类型枚举
 * <p>
 * 定义系统支持的IM消息格式
 * </p>
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
