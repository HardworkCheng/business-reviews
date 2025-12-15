package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 优惠券Mapper接口
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
    
    /**
     * 扣减库存
     */
    @Update("UPDATE coupons SET remain_count = remain_count - 1 WHERE id = #{couponId} AND remain_count > 0")
    int decrementStock(@Param("couponId") Long couponId);
    
    /**
     * 增加库存
     */
    @Update("UPDATE coupons SET remain_count = remain_count + 1 WHERE id = #{couponId}")
    int incrementStock(@Param("couponId") Long couponId);
}
