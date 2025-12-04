package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记话题关联实体类
 */
@Data
@TableName("note_topics")
public class NoteTopic implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long noteId;
    
    private Long topicId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
