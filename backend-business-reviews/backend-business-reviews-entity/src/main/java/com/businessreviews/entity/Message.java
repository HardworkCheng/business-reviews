package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Message {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long senderId;
    
    private Long receiverId;
    
    private String content;
    
    private Integer type; // 1=文本, 2=图片
    
    private Boolean isRead;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
