package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏笔记实体类
 */
@Data
@TableName("user_note_bookmarks")
public class UserNoteBookmark implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long noteId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
