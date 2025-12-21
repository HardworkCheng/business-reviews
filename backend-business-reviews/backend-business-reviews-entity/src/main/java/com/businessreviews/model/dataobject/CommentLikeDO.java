package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论点赞数据对象
 */
@Data
@TableName("user_comment_likes")
public class CommentLikeDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long commentId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
