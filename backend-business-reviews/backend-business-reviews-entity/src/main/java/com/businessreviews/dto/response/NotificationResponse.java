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
}
