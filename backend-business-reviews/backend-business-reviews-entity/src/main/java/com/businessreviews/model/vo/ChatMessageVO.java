package com.businessreviews.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息详情对象
 * <p>
 * 用于聊天对话框内展示单条消息详情
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class ChatMessageVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Integer messageType;

    /**
     * 是否已读
     */
    @JsonProperty("isRead")
    private Boolean readStatus;

    private LocalDateTime createdAt;
}
