package com.businessreviews.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息详情对象
 * <p>
 * 专用于私信场景，包含发送/接收双方信息及消息归属标识（是否为当前用户发送）
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class PrivateMessageVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long id;

    /** 发送者ID */
    private Long senderId;

    /** 发送者用户名 */
    private String senderUsername;

    /** 发送者头像 */
    private String senderAvatar;

    /** 接收者ID */
    private Long receiverId;

    /** 消息内容 */
    private String content;

    /** 消息类型：1=文本，2=图片，3=语音 */
    private Integer messageType;

    /** 是否已读 */
    @JsonProperty("isRead")
    private Boolean readStatus;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 是否是我发送的 */
    @JsonProperty("isMine")
    private Boolean mine;
}
