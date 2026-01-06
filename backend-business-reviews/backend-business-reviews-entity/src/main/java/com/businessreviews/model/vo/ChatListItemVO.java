package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天列表项展示对象
 * <p>
 * 用于消息列表页展示最近联系人、最后一条消息摘要及未读数
 * </p>
 * 
 * @author businessreviews
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
