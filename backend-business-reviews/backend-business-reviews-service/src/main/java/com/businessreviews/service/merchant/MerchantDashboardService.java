package com.businessreviews.service.merchant;

import java.util.Map;

/**
 * 商家数据看板服务接口
 * <p>
 * 提供商家运营中心的数据统计和分析功能
 * </p>
 * 
 * @author businessreviews
 */
public interface MerchantDashboardService {

    /**
     * 获取数据看板数据
     * 
     * @param merchantId 商家ID
     * @return 看板统计数据MAP
     */
    Map<String, Object> getDashboardData(Long merchantId);

    /**
     * 获取笔记分析数据
     * 
     * @param merchantId 商家ID
     * @param startDate  开始日期(yyyy-MM-dd)
     * @param endDate    结束日期(yyyy-MM-dd)
     * @return 笔记分析数据MAP
     */
    Map<String, Object> getNoteAnalytics(Long merchantId, String startDate, String endDate);

    /**
     * 获取优惠券分析数据
     * 
     * @param merchantId 商家ID
     * @param startDate  开始日期(yyyy-MM-dd)
     * @param endDate    结束日期(yyyy-MM-dd)
     * @return 优惠券分析数据MAP
     */
    Map<String, Object> getCouponAnalytics(Long merchantId, String startDate, String endDate);

    /**
     * 获取用户分析数据
     * 
     * @param merchantId 商家ID
     * @param startDate  开始日期(yyyy-MM-dd)
     * @param endDate    结束日期(yyyy-MM-dd)
     * @return 用户分析数据MAP
     */
    Map<String, Object> getUserAnalytics(Long merchantId, String startDate, String endDate);
}
