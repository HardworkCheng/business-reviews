package com.businessreviews.service.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.merchant.CreateCouponDTO;
import com.businessreviews.model.vo.CouponDetailVO;
import com.businessreviews.model.vo.CouponItemVO;
import java.util.Map;

/**
 * 商家优惠券服务接口
 * <p>
 * 提供商家运营中心的优惠券管理功能
 * </p>
 * 
 * @author businessreviews
 */
public interface MerchantCouponService {

    /**
     * 获取优惠券列表
     * 
     * @param merchantId 商家ID
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param type       类型过滤
     * @param status     状态过滤
     * @param shopId     店铺ID过滤
     * @return 优惠券列表分页数据
     */
    PageResult<CouponItemVO> getCouponList(Long merchantId, Integer pageNum, Integer pageSize, Integer type,
            Integer status, Long shopId);

    /**
     * 获取优惠券详情
     * 
     * @param merchantId 商家ID
     * @param couponId   优惠券ID
     * @return 优惠券详情VO
     */
    CouponDetailVO getCouponDetail(Long merchantId, Long couponId);

    /**
     * 创建优惠券
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param request    创建请求对象
     * @return 新增优惠券ID
     */
    Long createCoupon(Long merchantId, Long operatorId, CreateCouponDTO request);

    /**
     * 更新优惠券
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param couponId   优惠券ID
     * @param request    更新请求对象
     */
    void updateCoupon(Long merchantId, Long operatorId, Long couponId, CreateCouponDTO request);

    /**
     * 更新优惠券状态
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param couponId   优惠券ID
     * @param status     新状态
     */
    void updateCouponStatus(Long merchantId, Long operatorId, Long couponId, Integer status);

    /**
     * 获取优惠券统计
     * 
     * @param merchantId 商家ID
     * @param couponId   优惠券ID
     * @return 统计数据MAP
     */
    Map<String, Object> getCouponStats(Long merchantId, Long couponId);

    /**
     * 核销优惠券
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID(核销员)
     * @param couponId   优惠券ID
     * @param code       券码
     * @param shopId     核销店铺ID
     */
    void redeemCoupon(Long merchantId, Long operatorId, Long couponId, String code, Long shopId);

    /**
     * 验证券码
     * 
     * @param merchantId 商家ID
     * @param code       券码
     * @return 验证结果MAP
     */
    Map<String, Object> verifyCoupon(Long merchantId, String code);
}
