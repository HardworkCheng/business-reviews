package com.businessreviews.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商家查询参数对象
 * <p>
 * 封装商家列表的筛选条件，支持位置距离、评分、价格区间及分类查询
 * </p>
 * 
 * @author businessreviews
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词（商家名称/地址）
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 状态筛选（1营业中，2休息中，3已关闭）
     */
    private Integer status;

    /**
     * 最低评分
     */
    private BigDecimal minRating;

    /**
     * 最低人均消费
     */
    private BigDecimal minPrice;

    /**
     * 最高人均消费
     */
    private BigDecimal maxPrice;

    /**
     * 用户纬度（用于距离计算）
     */
    private BigDecimal latitude;

    /**
     * 用户经度（用于距离计算）
     */
    private BigDecimal longitude;

    /**
     * 最大距离（米）
     */
    private Integer maxDistance;

    /**
     * 排序字段（rating/distance/popularity）
     */
    private String sortBy;

    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder;
}
