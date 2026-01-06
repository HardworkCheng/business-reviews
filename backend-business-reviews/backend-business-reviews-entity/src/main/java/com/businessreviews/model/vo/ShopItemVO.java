package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家卡片展示对象
 * <p>
 * 用于首页推荐或搜索结果展示，包含商家封面、评分、人均等核心信息
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class ShopItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
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
