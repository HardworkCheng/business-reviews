package com.businessreviews.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShopItemResponse {
    private String id;
    private String name;
    private String image;
    private BigDecimal rating;
    private Integer avgPrice;
    private String address;
    private String category;
    private Integer noteCount;
    private String distance;
    private String phone;
    private Integer status;
    private String businessHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
