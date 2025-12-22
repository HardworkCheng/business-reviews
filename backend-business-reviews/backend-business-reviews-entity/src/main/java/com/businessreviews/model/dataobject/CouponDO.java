package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券数据对象（DO）
 * 与数据库表coupons一一对应
 * 
 * @author businessreviews
 */
@Data
@TableName("coupons")
public class CouponDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属商家ID
     */
    private Long merchantId;
    
    /**
     * 适用门店ID（空表示全部门店）
     */
    private Long shopId;
    
    /**
     * 券类型（1现金券，2折扣券，3专属券，4新人券）
     */
    private Integer type;
    
    /**
     * 券标题
     */
    private String title;
    
    /**
     * 券描述
     */
    private String description;
    
    /**
     * 优惠金额（现金券）
     */
    private BigDecimal amount;
    
    /**
     * 折扣率（折扣券，如0.85表示85折）
     */
    private BigDecimal discount;
    
    /**
     * 最低消费金额
     */
    private BigDecimal minAmount;
    
    /**
     * 发放总量
     */
    private Integer totalCount;
    
    /**
     * 剩余数量
     */
    private Integer remainCount;
    
    /**
     * 每日领取限制（数据库中不存在此字段）
     */
    @TableField(exist = false)
    private Integer dailyLimit;
    
    /**
     * 每人限领次数
     */
    private Integer perUserLimit;
    
    /**
     * 生效时间
     */
    private LocalDateTime startTime;
    
    /**
     * 失效时间
     */
    private LocalDateTime endTime;
    
    /**
     * 可使用开始时间（数据库中不存在此字段，仅用于业务逻辑）
     */
    @TableField(exist = false)
    private LocalDateTime useStartTime;
    
    /**
     * 可使用结束时间（数据库中不存在此字段，仅用于业务逻辑）
     */
    @TableField(exist = false)
    private LocalDateTime useEndTime;
    
    /**
     * 适用品类（JSON）（数据库中不存在此字段）
     */
    @TableField(exist = false)
    private String applicableCategories;
    
    /**
     * 是否可叠加（0否，1是）
     * 注意：按照阿里巴巴规范，使用Integer而非Boolean
     */
    private Integer stackable;
    
    /**
     * 状态（1启用，2停用，3已结束）
     */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
