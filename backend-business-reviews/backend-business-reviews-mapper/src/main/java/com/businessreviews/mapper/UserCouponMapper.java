package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户优惠券Mapper接口
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    
    /**
     * 根据券码查询
     */
    @Select("SELECT * FROM user_coupons WHERE code = #{code}")
    UserCoupon selectByCode(@Param("code") String code);
    
    /**
     * 统计用户已领取某优惠券的数量
     */
    @Select("SELECT COUNT(*) FROM user_coupons WHERE user_id = #{userId} AND coupon_id = #{couponId}")
    int countUserCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);
}
