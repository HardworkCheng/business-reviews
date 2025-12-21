package com.businessreviews.web.merchant;

import com.businessreviews.common.Result;
import com.businessreviews.merchant.context.MerchantContext;
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
}
