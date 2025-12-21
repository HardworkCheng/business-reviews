package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天列表项VO
 */
@Data
public class ChatListItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String name;
    
    private Long userId;
    
    private String avatar;
    
    private String lastMessage;
    
    private String time;
    
    private Integer unread;
    
    private Boolean online;
}
