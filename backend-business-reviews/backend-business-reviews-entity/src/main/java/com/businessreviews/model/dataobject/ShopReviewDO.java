package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家评价数据对象
 */
@Data
@TableName("shop_reviews")
public class ShopReviewDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商家ID
     */
    private Long shopId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 综合评分（1-5）
     */
    private BigDecimal rating;
    
    /**
     * 口味评分
     */
    private BigDecimal tasteScore;
    
    /**
     * 环境评分
     */
    private BigDecimal environmentScore;
    
    /**
     * 服务评分
     */
    private BigDecimal serviceScore;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 评价图片（JSON）
     */
    private String images;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 状态（1正常，2隐藏）
     */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
