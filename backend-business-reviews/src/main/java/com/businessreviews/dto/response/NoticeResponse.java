package com.businessreviews.dto.response;

import lombok.Data;

/**
 * 系统通知响应
 */
@Data
public class NoticeResponse {
    
    private Long id;
    
    private String user;
    
    private Long userId;
    
    private String avatar;
    
    private String action;
    
    private String time;
    
    private String icon;
    
    private String type;  // like/comment/follow
    
    private String image;
    
    private Long targetId;
    
    private Boolean isRead;
}
