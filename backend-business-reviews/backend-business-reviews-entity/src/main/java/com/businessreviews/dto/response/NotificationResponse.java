package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class NotificationResponse {
    private String id;
    private String title;
    private String content;
    private Integer type;
    private String relatedId;
    private Boolean isRead;
    private String createdAt;
    private String timeAgo;
    
    // 发送者信息
    private Long fromUserId;
    private String fromUsername;
    private String fromAvatar;
    
    // 笔记信息
    private Long noteId;
    private String noteTitle;
    private String noteImage;
}
