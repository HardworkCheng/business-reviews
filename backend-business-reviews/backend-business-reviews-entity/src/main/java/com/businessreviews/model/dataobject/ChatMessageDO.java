package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息数据映射对象
 * <p>
 * 对应数据库表 chat_messages，存储点对点聊天记录
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("chat_messages")
public class ChatMessageDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    /**
     * 消息类型（1文本，2图片，3语音）
     */
    private Integer messageType;

    /**
     * 是否已读（0未读，1已读）
     */
    @TableField("is_read")
    private Integer readStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
