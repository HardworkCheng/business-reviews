package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记标签实体类
 */
@Data
@TableName("note_tags")
public class NoteTag implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 笔记ID
     */
    private Long noteId;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
