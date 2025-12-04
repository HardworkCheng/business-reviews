package com.businessreviews.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家详情响应
 */
@Data
public class ShopDetailResponse {
    
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
    
    private Boolean isFavorited;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private String distance;
}
