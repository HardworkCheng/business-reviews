package com.businessreviews.service.ai;

import com.businessreviews.model.dto.ai.WeeklyReportDTO;

/**
 * 评论分析服务接口
 * 
 * @author businessreviews
 */
public interface ReviewAnalysisService {

    /**
     * 生成商家周报
     * 
     * @param shopId 商家门店ID
     * @return 周报分析结果
     */
    WeeklyReportDTO generateReport(Long shopId);
}
