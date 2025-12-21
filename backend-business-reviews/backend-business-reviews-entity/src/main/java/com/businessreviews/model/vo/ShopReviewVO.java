package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 商家评价VO
 */
@Data
public class ShopReviewVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
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
