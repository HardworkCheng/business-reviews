package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关注关系数据映射对象
 * <p>
 * 对应数据库表 user_follows，记录用户之间的关注关系
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("user_follows")
public class UserFollowDO implements Serializable {

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
