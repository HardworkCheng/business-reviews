package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商家视图对象（VO）
 * 用于Web层返回给前端展示
 * 
 * @author businessreviews
 */
@Data
public class ShopVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商家ID（字符串类型，避免前端精度丢失）
     */
    private String shopId;
    
    /**
     * 商家名称
     */
    private String name;
    
    /**
     * 封面图URL
     */
    private String headerImage;
    
    /**
     * 商家图片列表
     */
    private List<String> images;
    
    /**
     * 商家描述
     */
    private String description;
    
    /**
     * 联系电话（可能需要脱敏）
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
     * 状态文本（营业中/休息中/已关闭）
     */
    private String statusText;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 距离（米）
     */
    private String distance;
    
    /**
     * 是否已收藏
     */
    private Boolean favorited;
}
