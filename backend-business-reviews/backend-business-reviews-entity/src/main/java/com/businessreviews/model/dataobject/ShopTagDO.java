package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家标签数据对象
 */
@Data
@TableName("shop_tags")
public class ShopTagDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商家ID
     */
    private Long shopId;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
