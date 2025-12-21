package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会话VO
 */
@Data
public class ConversationVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String userId;
    private String username;
    private String avatar;
    private String lastMessage;
    private String lastTime;
    private Integer unreadCount;
}
