package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.CouponDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 优惠券Mapper接口
 * 合并自 CouponMapper 和 CouponDOMapper
 * 
 * @author businessreviews
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {
    
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
    
    /**
     * 根据商家ID查询优惠券列表
     * 
     * @param merchantId 商家ID
     * @return 优惠券DO列表
     */
    @Select("SELECT * FROM coupons WHERE merchant_id = #{merchantId} AND status = 1 ORDER BY created_at DESC")
    List<CouponDO> listByMerchantId(@Param("merchantId") Long merchantId);
}
