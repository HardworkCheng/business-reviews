package com.businessreviews.service.ai;

import com.businessreviews.model.dto.ai.WeeklyReportDTO;

/**
 * 评论分析服务接口
 * <p>
 * 提供商家运营中心的评论数据分析与周报生成功能
 * </p>
 * 
 * @author businessreviews
 */
public interface ReviewAnalysisService {

    /**
     * 生成商家周报
     * 
     * @param shopId 商家门店ID
     * @return 周报分析结果DTO
     */
    WeeklyReportDTO generateReport(Long shopId);
}
