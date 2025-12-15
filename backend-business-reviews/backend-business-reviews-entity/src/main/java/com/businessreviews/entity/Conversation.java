package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话实体类
 */
@Data
@TableName("conversations")
public class Conversation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户1的ID（较小的ID） */
    private Long user1Id;
    
    /** 用户2的ID（较大的ID） */
    private Long user2Id;
    
    /** 最后一条消息ID */
    private Long lastMessageId;
    
    /** 最后一条消息内容 */
    private String lastMessageContent;
    
    /** 最后一条消息时间 */
    private LocalDateTime lastMessageTime;
    
    /** 用户1的未读消息数 */
    private Integer user1UnreadCount;
    
    /** 用户2的未读消息数 */
    private Integer user2UnreadCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
