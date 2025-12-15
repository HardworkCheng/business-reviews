package com.businessreviews.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话列表项响应
 */
@Data
public class ConversationItemResponse {
    
    /** 会话ID */
    private Long conversationId;
    
    /** 对方用户ID */
    private Long otherUserId;
    
    /** 对方用户名 */
    private String otherUsername;
    
    /** 对方头像 */
    private String otherAvatar;
    
    /** 最后一条消息内容 */
    private String lastMessageContent;
    
    /** 最后一条消息时间 */
    private LocalDateTime lastMessageTime;
    
    /** 未读消息数 */
    private Integer unreadCount;
    
    /** 对方是否在线 */
    private Boolean isOnline;
}
