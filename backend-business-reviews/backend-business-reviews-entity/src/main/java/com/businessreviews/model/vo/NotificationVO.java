package com.businessreviews.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通知VO
 */
@Data
public class NotificationVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String title;
    private String content;
    private Integer type;
    private String relatedId;
    
    /**
     * 是否已读
     */
    @JsonProperty("isRead")
    private Boolean readStatus;
    
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
