package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户领券记录实体类
 */
@Data
@TableName("user_coupons")
public class UserCoupon {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 优惠券ID */
    private Long couponId;
    
    /** 用户ID */
    private Long userId;
    
    /** 券码（唯一） */
    private String code;
    
    /** 状态（1未使用，2已使用，3已过期） */
    private Integer status;
    
    /** 领取时间 */
    private LocalDateTime receiveTime;
    
    /** 使用时间 */
    private LocalDateTime useTime;
    
    /** 使用门店ID */
    private Long useShopId;
    
    /** 核销操作员ID */
    private Long operatorId;
    
    private LocalDateTime createdAt;
}
