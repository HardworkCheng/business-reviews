package com.businessreviews.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息展示对象
 * <p>
 * 通用消息结构，包含文本内容或JSON格式的扩展数据（如笔记分享）
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class MessageVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String receiverId;
    private String content;
    private Integer type;

    /**
     * 笔记数据（JSON格式，用于笔记分享消息）
     */
    private String noteData;

    /**
     * 是否已读
     */
    @JsonProperty("isRead")
    private Boolean readStatus;

    private String createdAt;
    private String timeAgo;
}
