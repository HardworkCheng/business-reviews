package com.businessreviews.web.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.model.dto.merchant.CreateCouponDTO;
import com.businessreviews.model.vo.CouponDetailVO;
import com.businessreviews.model.vo.CouponItemVO;
import com.businessreviews.merchant.context.MerchantContext;
import com.businessreviews.service.merchant.MerchantCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家优惠券管理控制器
 * <p>
 * 提供商家对优惠券的管理功能：
 * - 创建、更新、查询优惠券
 * - 启用/停用优惠券
 * - 核销优惠券、验证券码
 * </p>
 *
 * @author businessreviews
 */
@RestController
@RequestMapping("/merchant/coupons")
@RequiredArgsConstructor
public class MerchantCouponController {

    private final MerchantCouponService merchantCouponService;

    /**
     * 获取优惠券列表
     */
    @GetMapping
    public Result<PageResult<CouponItemVO>> getCouponList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long shopId) {
        Long merchantId = MerchantContext.requireMerchantId();
        PageResult<CouponItemVO> result = merchantCouponService.getCouponList(merchantId, pageNum, pageSize, type,
                status, shopId);
        return Result.success(result);
    }

    /**
     * 获取优惠券详情
     *
     * @param id 优惠券ID
     * @return 优惠券详情
     */
    @GetMapping("/{id}")
    public Result<CouponDetailVO> getCouponDetail(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        CouponDetailVO response = merchantCouponService.getCouponDetail(merchantId, id);
        return Result.success(response);
    }

    /**
     * 创建优惠券
     *
     * @param request 创建参数
     * @return 创建结果(ID)
     */
    @PostMapping
    public Result<Map<String, Long>> createCoupon(@RequestBody @Valid CreateCouponDTO request) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        Long couponId = merchantCouponService.createCoupon(merchantId, operatorId, request);
        return Result.success("创建成功", Map.of("couponId", couponId));
    }

    /**
     * 更新优惠券
     *
     * @param id      优惠券ID
     * @param request 更新参数
     * @return 成功结果
     */
    @PutMapping("/{id}")
    public Result<?> updateCoupon(@PathVariable Long id, @RequestBody @Valid CreateCouponDTO request) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantCouponService.updateCoupon(merchantId, operatorId, id, request);
        return Result.success("更新成功");
    }

    /**
     * 更新优惠券状态（启用/停用）
     *
     * @param id     优惠券ID
     * @param status 状态
     * @return 成功结果
     */
    @PutMapping("/{id}/status")
    public Result<?> updateCouponStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantCouponService.updateCouponStatus(merchantId, operatorId, id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 获取优惠券统计数据
     *
     * @param id 优惠券ID
     * @return 统计数据
     */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getCouponStats(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> stats = merchantCouponService.getCouponStats(merchantId, id);
        return Result.success(stats);
    }

    /**
     * 核销优惠券
     *
     * @param id     优惠券ID
     * @param code   核销码
     * @param shopId 门店ID
     * @return 成功结果
     */
    @PostMapping("/{id}/redeem")
    public Result<?> redeemCoupon(@PathVariable Long id, @RequestParam String code, @RequestParam Long shopId) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantCouponService.redeemCoupon(merchantId, operatorId, id, code, shopId);
        return Result.success("核销成功");
    }

    /**
     * 根据券码查询优惠券信息
     *
     * @param code 核销码
     * @return 优惠券核销信息
     */
    @GetMapping("/verify")
    public Result<Map<String, Object>> verifyCoupon(@RequestParam String code) {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> result = merchantCouponService.verifyCoupon(merchantId, code);
        return Result.success(result);
    }
}
