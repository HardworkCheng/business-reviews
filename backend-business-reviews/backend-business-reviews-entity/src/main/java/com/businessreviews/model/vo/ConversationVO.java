package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 私信会话简要信息对象
 * <p>
 * 包含会话基础信息及未读计数
 * </p>
 * 
 * @author businessreviews
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
