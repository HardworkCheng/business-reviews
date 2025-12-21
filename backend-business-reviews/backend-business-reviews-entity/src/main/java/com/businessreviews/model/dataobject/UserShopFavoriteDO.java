package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏商家数据对象
 */
@Data
@TableName("user_shop_favorites")
public class UserShopFavoriteDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 商家ID
     */
    private Long shopId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
