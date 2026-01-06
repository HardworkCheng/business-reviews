package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户点赞笔记数据映射对象
 * <p>
 * 对应数据库表 user_note_likes，记录用户对笔记的点赞操作
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("user_note_likes")
public class UserNoteLikeDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long noteId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
