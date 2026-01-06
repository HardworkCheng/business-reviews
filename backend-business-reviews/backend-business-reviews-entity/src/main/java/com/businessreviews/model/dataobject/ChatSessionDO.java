package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话数据映射对象
 * <p>
 * 对应数据库表 chat_sessions，管理用户间的私信会话状态
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("chat_sessions")
public class ChatSessionDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long otherUserId;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private Integer unreadCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
