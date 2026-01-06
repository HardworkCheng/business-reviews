package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户领券记录数据映射对象
 * <p>
 * 对应数据库表 user_coupons，记录用户领取的优惠券及其使用状态
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("user_coupons")
public class UserCouponDO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    /** 创建时间（数据库中不存在此字段，使用receiveTime代替） */
    @TableField(exist = false)
    private LocalDateTime createdAt;
}
