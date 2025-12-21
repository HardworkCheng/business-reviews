package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统通知数据对象
 */
@Data
@TableName("system_notices")
public class SystemNoticeDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 接收通知的用户ID
     */
    private Long userId;
    
    /**
     * 触发通知的用户ID
     */
    private Long fromUserId;
    
    /**
     * 通知类型（1点赞笔记，2评论笔记，3关注，4点赞评论）
     */
    private Integer noticeType;
    
    /**
     * 目标ID（笔记ID或评论ID）
     */
    private Long targetId;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 关联图片URL
     */
    private String imageUrl;
    
    /**
     * 是否已读（0未读，1已读）
     */
    @TableField("is_read")
    private Integer readStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
