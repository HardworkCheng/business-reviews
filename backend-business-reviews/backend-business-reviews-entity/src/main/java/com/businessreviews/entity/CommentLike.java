package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_comment_likes")
public class CommentLike {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long commentId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
