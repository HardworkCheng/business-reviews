package com.businessreviews.dto.response;

import lombok.Data;

/**
 * 聊天列表项响应
 */
@Data
public class ChatListItemResponse {
    
    private Long id;
    
    private String name;
    
    private Long userId;
    
    private String avatar;
    
    private String lastMessage;
    
    private String time;
    
    private Integer unread;
    
    private Boolean online;
}
