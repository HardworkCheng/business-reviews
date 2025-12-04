package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关注关系实体类
 */
@Data
@TableName("user_follows")
public class UserFollow implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关注者ID
     */
    private Long userId;
    
    /**
     * 被关注者ID
     */
    private Long followUserId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
