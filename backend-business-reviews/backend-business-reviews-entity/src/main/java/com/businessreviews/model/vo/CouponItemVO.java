package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券列表项VO
 */
@Data
public class CouponItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 优惠券ID */
    private String id;
    
    /** 券类型（1现金券，2折扣券，3专属券，4新人券） */
    private Integer type;
    
    /** 券类型名称 */
    private String typeName;
    
    /** 券标题 */
    private String title;
    
    /** 优惠金额（现金券） */
    private BigDecimal amount;
    
    /** 折扣率（折扣券） */
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
    
    /** 适用门店名称 */
    private String shopName;
    
    /** 状态（1启用，2停用，3已结束） */
    private Integer status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 生效时间 */
    private String startTime;
    
    /** 失效时间 */
    private String endTime;
    
    /** 创建时间 */
    private String createdAt;
}
