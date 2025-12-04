package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class ConversationResponse {
    private String id;
    private String userId;
    private String username;
    private String avatar;
    private String lastMessage;
    private String lastTime;
    private Integer unreadCount;
}
