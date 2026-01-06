package com.businessreviews.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话列表项对象
 * <p>
 * (同ChatListItemVO，用于不同场景的列表展示)
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class ConversationItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
    @JsonProperty("isOnline")
    private Boolean online;
}
