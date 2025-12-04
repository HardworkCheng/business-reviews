package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_favorites")
public class UserFavorite {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Integer type; // 1=笔记, 2=商家
    
    private Long targetId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
