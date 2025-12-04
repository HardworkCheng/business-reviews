package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private Integer type; // 1=点赞, 2=评论, 3=关注, 4=系统
    
    private Long relatedId;
    
    private Boolean isRead;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
