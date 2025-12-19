package com.businessreviews.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShopItemResponse {
    private String id;
    private Long merchantId;
    private Integer categoryId;
    private String name;
    private String image;
    private String headerImage;
    private String images;
    private String description;
    private BigDecimal rating;
    private BigDecimal tasteScore;
    private BigDecimal environmentScore;
    private BigDecimal serviceScore;
    private Integer reviewCount;
    private Integer popularity;
    private Integer avgPrice;
    private BigDecimal averagePrice;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String category;
    private Integer noteCount;
    private String distance;
    private String phone;
    private Integer status;
    private String businessHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
