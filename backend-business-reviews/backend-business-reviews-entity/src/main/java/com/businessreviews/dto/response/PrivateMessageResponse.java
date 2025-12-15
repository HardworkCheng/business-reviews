package com.businessreviews.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 私信消息响应
 */
@Data
public class PrivateMessageResponse {
    
    /** 消息ID */
    private Long id;
    
    /** 发送者ID */
    private Long senderId;
    
    /** 发送者用户名 */
    private String senderUsername;
    
    /** 发送者头像 */
    private String senderAvatar;
    
    /** 接收者ID */
    private Long receiverId;
    
    /** 消息内容 */
    private String content;
    
    /** 消息类型：1=文本，2=图片，3=语音 */
    private Integer messageType;
    
    /** 是否已读 */
    private Boolean isRead;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 是否是我发送的 */
    private Boolean isMine;
}
