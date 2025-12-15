package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_browse_history")
public class BrowseHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    @TableField("target_type")
    private Integer type; // 1=笔记, 2=商家
    
    private Long targetId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
