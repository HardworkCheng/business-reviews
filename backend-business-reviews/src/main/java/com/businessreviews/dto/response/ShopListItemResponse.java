package com.businessreviews.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家列表项响应
 */
@Data
public class ShopListItemResponse {
    
    private Long id;
    
    private String name;
    
    private String image;
    
    private BigDecimal rating;
    
    private Integer reviewCount;
    
    private List<String> tags;
    
    private String location;
    
    private String address;
    
    private String distance;
    
    private Integer popularity;
    
    private BigDecimal averagePrice;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
}
