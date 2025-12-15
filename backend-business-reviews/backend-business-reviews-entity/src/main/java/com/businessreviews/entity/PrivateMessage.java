package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息实体类
 */
@Data
@TableName("private_messages")
public class PrivateMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 会话ID */
    private Long conversationId;
    
    /** 发送者ID */
    private Long senderId;
    
    /** 接收者ID */
    private Long receiverId;
    
    /** 消息内容 */
    private String content;
    
    /** 消息类型：1=文本，2=图片，3=语音 */
    private Integer messageType;
    
    /** 是否已读：0=未读，1=已读 */
    private Integer isRead;
    
    /** 已读时间 */
    private LocalDateTime readAt;
    
    /** 发送者是否删除：0=否，1=是 */
    private Integer isDeletedBySender;
    
    /** 接收者是否删除：0=否，1=是 */
    private Integer isDeletedByReceiver;
    
    private LocalDateTime createdAt;
}
