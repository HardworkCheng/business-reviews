package com.businessreviews.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 商家评价响应
 */
@Data
public class ShopReviewResponse {
    
    private Long id;
    
    private String author;
    
    private Long authorId;
    
    private String avatar;
    
    private BigDecimal rating;
    
    private BigDecimal tasteScore;
    
    private BigDecimal environmentScore;
    
    private BigDecimal serviceScore;
    
    private LocalDate date;
    
    private String content;
    
    private List<String> images;
    
    private Integer likeCount;
}
