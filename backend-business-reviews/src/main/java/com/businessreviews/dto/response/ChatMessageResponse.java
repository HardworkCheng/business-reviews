package com.businessreviews.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息响应
 */
@Data
public class ChatMessageResponse {
    
    private Long id;
    
    private Long fromUserId;
    
    private Long toUserId;
    
    private String content;
    
    private Integer messageType;
    
    private Boolean isRead;
    
    private LocalDateTime createdAt;
}
