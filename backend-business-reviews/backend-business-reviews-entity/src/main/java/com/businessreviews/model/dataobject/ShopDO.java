package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家信息数据对象（DO）
 * 与数据库表shops一一对应
 * 
 * @author businessreviews
 */
@Data
@TableName("shops")
public class ShopDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属商家ID
     */
    private Long merchantId;
    
    /**
     * 分类ID
     */
    private Integer categoryId;
    
    /**
     * 商家名称
     */
    private String name;
    
    /**
     * 封面图URL
     */
    private String headerImage;
    
    /**
     * 商家图片集合（JSON）
     */
    private String images;
    
    /**
     * 商家描述
     */
    private String description;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 详细地址
     */
    private String address;

    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 营业时间
     */
    private String businessHours;
    
    /**
     * 人均消费
     */
    private BigDecimal averagePrice;
    
    /**
     * 综合评分
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
     * 评价数量
     */
    private Integer reviewCount;
    
    /**
     * 人气值
     */
    private Integer popularity;
    
    /**
     * 状态（1营业中，2休息中，3已关闭）
     */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
