package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class MessageResponse {
    private String id;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String receiverId;
    private String content;
    private Integer type;
    private Boolean isRead;
    private String createdAt;
    private String timeAgo;
}
