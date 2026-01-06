package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户评论点赞数据映射对象
 * <p>
 * 对应数据库表 user_comment_likes，记录用户对评论的点赞操作
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("user_comment_likes")
public class UserCommentLikeDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long commentId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
