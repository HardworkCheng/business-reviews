package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@TableName("chat_messages")
public class ChatMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long sessionId;
    
    private Long fromUserId;
    
    private Long toUserId;
    
    private String content;
    
    /**
     * 消息类型（1文本，2图片，3语音）
     */
    private Integer messageType;
    
    /**
     * 是否已读（0未读，1已读）
     */
    private Integer isRead;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
