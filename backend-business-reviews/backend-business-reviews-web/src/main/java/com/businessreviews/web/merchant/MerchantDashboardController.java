package com.businessreviews.web.merchant;

import com.businessreviews.common.Result;
import com.businessreviews.merchant.context.MerchantContext;
import com.businessreviews.model.dto.ai.WeeklyReportDTO;
import com.businessreviews.service.ai.ReviewAnalysisService;
import com.businessreviews.service.merchant.MerchantDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家数据看板控制器
 */
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantDashboardController {

    private final MerchantDashboardService merchantDashboardService;
    private final ReviewAnalysisService reviewAnalysisService;

    /**
     * 获取数据看板数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> data = merchantDashboardService.getDashboardData(merchantId);
        return Result.success(data);
    }

    /**
     * 获取笔记分析数据
     */
    @GetMapping("/analytics/notes")
    public Result<Map<String, Object>> getNoteAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> data = merchantDashboardService.getNoteAnalytics(merchantId, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取优惠券分析数据
     */
    @GetMapping("/analytics/coupons")
    public Result<Map<String, Object>> getCouponAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> data = merchantDashboardService.getCouponAnalytics(merchantId, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 获取用户分析数据
     */
    @GetMapping("/analytics/users")
    public Result<Map<String, Object>> getUserAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> data = merchantDashboardService.getUserAnalytics(merchantId, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 生成商家评论口碑周报
     * 使用 AI 分析过去 7 天的评论数据，生成结构化的周报
     * 
     * @param shopId 门店ID
     * @return AI 生成的周报分析结果
     */
    @GetMapping("/analytics/weekly-report/{shopId}")
    public Result<WeeklyReportDTO> generateWeeklyReport(@PathVariable Long shopId) {
        // 验证商家身份（确保已登录）
        MerchantContext.requireMerchantId();

        WeeklyReportDTO report = reviewAnalysisService.generateReport(shopId);
        return Result.success(report);
    }
}
