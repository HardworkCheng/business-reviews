package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商家详情VO
 */
@Data
public class ShopDetailVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String name;
    
    private String headerImage;
    
    private List<String> images;
    
    private String description;
    
    private BigDecimal rating;
    
    private Integer reviewCount;
    
    private BigDecimal tasteScore;
    
    private BigDecimal environmentScore;
    
    private BigDecimal serviceScore;
    
    private String address;
    
    private String businessHours;
    
    private String phone;
    
    private BigDecimal averagePrice;
    
    private List<String> tags;
    
    /**
     * 是否已收藏
     * 注意：按照阿里巴巴规范，Boolean字段不使用is前缀
     * 使用@JsonProperty保持API兼容性
     */
    @com.fasterxml.jackson.annotation.JsonProperty("isFavorited")
    private Boolean favorited;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private String distance;
}
