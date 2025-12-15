package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建优惠券请求
 */
@Data
public class CreateCouponRequest {
    
    /** 适用门店ID（空表示全部门店） */
    private Long shopId;
    
    /** 券类型（1现金券，2折扣券，3专属券，4新人券） */
    @NotNull(message = "券类型不能为空")
    private Integer type;
    
    /** 券标题 */
    @NotBlank(message = "券标题不能为空")
    private String title;
    
    /** 券描述 */
    private String description;
    
    /** 优惠金额（现金券） */
    private BigDecimal amount;
    
    /** 折扣率（折扣券，如0.85表示85折） */
    private BigDecimal discount;
    
    /** 最低消费金额/满减门槛 */
    private BigDecimal minAmount;
    
    /** 满减门槛（别名） */
    private BigDecimal threshold;
    
    /** 折扣比例（前端传入，如85表示85折） */
    private Integer discountRate;
    
    /** 最高减免金额（折扣券） */
    private BigDecimal maxDiscount;
    
    /** 发放总量 */
    @NotNull(message = "发放总量不能为空")
    private Integer totalCount;
    
    /** 适用门店ID列表 */
    private List<Long> applicableShops;
    
    /** 每日领取限制 */
    private Integer dailyLimit;
    
    /** 每人限领次数 */
    private Integer perUserLimit;
    
    /** 生效时间 */
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime startTime;
    
    /** 失效时间 */
    @NotNull(message = "失效时间不能为空")
    private LocalDateTime endTime;
    
    /** 可使用开始时间 */
    private LocalDateTime useStartTime;
    
    /** 可使用结束时间 */
    private LocalDateTime useEndTime;
    
    /** 适用品类 */
    private List<Long> applicableCategories;
    
    /** 是否可叠加 */
    private Boolean stackable;
}
