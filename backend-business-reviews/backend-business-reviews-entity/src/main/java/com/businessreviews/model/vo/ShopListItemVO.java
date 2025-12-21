package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商家列表项VO
 */
@Data
public class ShopListItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
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
