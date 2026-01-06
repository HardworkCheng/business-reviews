package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏数据映射对象
 * <p>
 * 对应数据库表 user_favorites，记录用户的收藏行为
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("user_favorites")
public class UserFavoriteDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer type; // 1=笔记, 2=商家

    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
