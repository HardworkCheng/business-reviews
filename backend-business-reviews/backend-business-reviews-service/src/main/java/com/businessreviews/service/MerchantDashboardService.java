package com.businessreviews.service;

import java.util.Map;

/**
 * 商家数据看板服务接口
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
