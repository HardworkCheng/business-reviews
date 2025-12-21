package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券详情VO
 */
@Data
public class CouponDetailVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 优惠券ID */
    private String id;
    
    /** 券类型 */
    private Integer type;
    
    /** 券类型名称 */
    private String typeName;
    
    /** 券标题 */
    private String title;
    
    /** 券描述 */
    private String description;
    
    /** 优惠金额 */
    private BigDecimal amount;
    
    /** 折扣率 */
    private BigDecimal discount;
    
    /** 最低消费金额 */
    private BigDecimal minAmount;
    
    /** 发放总量 */
    private Integer totalCount;
    
    /** 剩余数量 */
    private Integer remainCount;
    
    /** 已领取数量 */
    private Integer claimedCount;
    
    /** 已使用数量 */
    private Integer usedCount;
    
    /** 每日领取限制 */
    private Integer dailyLimit;
    
    /** 每人限领次数 */
    private Integer perUserLimit;
    
    /** 适用门店ID */
    private String shopId;
    
    /** 适用门店名称 */
    private String shopName;
    
    /** 适用品类 */
    private List<Long> applicableCategories;
    
    /** 是否可叠加 */
    private Boolean stackable;
    
    /** 状态 */
    private Integer status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 生效时间 */
    private String startTime;
    
    /** 失效时间 */
    private String endTime;
    
    /** 可使用开始时间 */
    private String useStartTime;
    
    /** 可使用结束时间 */
    private String useEndTime;
    
    /** 创建时间 */
    private String createdAt;
}
