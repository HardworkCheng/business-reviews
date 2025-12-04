package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户点赞笔记实体类
 */
@Data
@TableName("user_note_likes")
public class UserNoteLike implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long noteId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
