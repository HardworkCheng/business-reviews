package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商家列表项展示对象
 * <p>
 * 用于分类列表页展示，包含商家评分、标签、距离及位置信息
 * </p>
 * 
 * @author businessreviews
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
