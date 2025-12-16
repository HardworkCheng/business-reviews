package com.businessreviews.service.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.dto.request.CreateCouponRequest;
import com.businessreviews.dto.response.CouponDetailResponse;
import com.businessreviews.dto.response.CouponItemResponse;
import java.util.Map;

/**
 * 商家优惠券服务接口
 * 提供商家运营中心的优惠券管理功能
 */
public interface MerchantCouponService {
    
    /**
     * 获取优惠券列表
     */
    PageResult<CouponItemResponse> getCouponList(Long merchantId, Integer pageNum, Integer pageSize, Integer type, Integer status, Long shopId);
    
    /**
     * 获取优惠券详情
     */
    CouponDetailResponse getCouponDetail(Long merchantId, Long couponId);
    
    /**
     * 创建优惠券
     */
    Long createCoupon(Long merchantId, Long operatorId, CreateCouponRequest request);
    
    /**
     * 更新优惠券
     */
    void updateCoupon(Long merchantId, Long operatorId, Long couponId, CreateCouponRequest request);
    
    /**
     * 更新优惠券状态
     */
    void updateCouponStatus(Long merchantId, Long operatorId, Long couponId, Integer status);
    
    /**
     * 获取优惠券统计
     */
    Map<String, Object> getCouponStats(Long merchantId, Long couponId);
    
    /**
     * 核销优惠券
     */
    void redeemCoupon(Long merchantId, Long operatorId, Long couponId, String code, Long shopId);
    
    /**
     * 验证券码
     */
    Map<String, Object> verifyCoupon(Long merchantId, String code);
}
