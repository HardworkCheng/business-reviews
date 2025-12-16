package com.businessreviews.service.merchant;

import java.util.Map;

/**
 * 商家数据看板服务接口
 * 提供商家运营中心的数据统计和分析功能
 */
public interface MerchantDashboardService {
    
    /**
     * 获取数据看板数据
     */
    Map<String, Object> getDashboardData(Long merchantId);
    
    /**
     * 获取笔记分析数据
     */
    Map<String, Object> getNoteAnalytics(Long merchantId, String startDate, String endDate);
    
    /**
     * 获取优惠券分析数据
     */
    Map<String, Object> getCouponAnalytics(Long merchantId, String startDate, String endDate);
    
    /**
     * 获取用户分析数据
     */
    Map<String, Object> getUserAnalytics(Long merchantId, String startDate, String endDate);
}
