package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记话题关联数据对象
 */
@Data
@TableName("note_topics")
public class NoteTopicDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long noteId;
    
    private Long topicId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
