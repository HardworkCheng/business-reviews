package com.businessreviews.web.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.model.vo.ShopDetailVO;
import com.businessreviews.model.vo.ShopItemVO;
import com.businessreviews.merchant.context.MerchantContext;
import com.businessreviews.service.merchant.MerchantShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家运营中心 - 门店管理控制器 (Web端)
 * <p>
 * 提供商家运营中心的门店管理API：
 * - GET /merchant/shops - 获取门店列表
 * - GET /merchant/shops/{id} - 获取门店详情
 * - POST /merchant/shops - 新增门店
 * - PUT /merchant/shops/{id} - 更新门店信息
 * - PUT /merchant/shops/{id}/status - 更新门店状态
 * - DELETE /merchant/shops/{id} - 删除门店
 * - GET /merchant/shops/{id}/stats - 获取门店统计数据
 * </p>
 *
 * @author businessreviews
 * @see com.businessreviews.service.merchant.MerchantShopService
 */
@Slf4j
@RestController
@RequestMapping("/merchant/shops")
@RequiredArgsConstructor
public class MerchantShopController {

    private final MerchantShopService merchantShopService;

    /**
     * 获取门店列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   状态
     * @param keyword  关键词
     * @return 门店列表
     */
    @GetMapping
    public Result<PageResult<ShopItemVO>> getShopList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Long merchantId = MerchantContext.requireMerchantId();
        PageResult<ShopItemVO> result = merchantShopService.getShopList(merchantId, pageNum, pageSize, status, keyword);
        return Result.success(result);
    }

    /**
     * 获取门店详情
     *
     * @param id 门店ID
     * @return 门店详情
     */
    @GetMapping("/{id}")
    public Result<ShopDetailVO> getShopDetail(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        ShopDetailVO response = merchantShopService.getShopDetail(merchantId, id);
        return Result.success(response);
    }

    /**
     * 新增门店
     *
     * @param request 门店信息
     * @return 成功结果
     */
    @PostMapping
    public Result<Map<String, Long>> createShop(@RequestBody @Valid Map<String, Object> request) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        Long shopId = merchantShopService.createShop(merchantId, operatorId, request);
        return Result.success("创建成功", Map.of("shopId", shopId));
    }

    /**
     * 更新门店信息
     *
     * @param id      门店ID
     * @param request 更新信息
     * @return 成功结果
     */
    @PutMapping("/{id}")
    public Result<?> updateShop(@PathVariable Long id, @RequestBody @Valid Map<String, Object> request) {
        log.info("收到更新店铺请求: shopId={}, 请求数据={}", id, request);
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        log.info("当前商家信息: merchantId={}, operatorId={}", merchantId, operatorId);
        merchantShopService.updateShop(merchantId, operatorId, id, request);
        log.info("店铺更新完成: shopId={}", id);
        return Result.success("更新成功");
    }

    /**
     * 更新门店状态（启用/停用）
     *
     * @param id     门店ID
     * @param status 状态
     * @return 成功结果
     */
    @PutMapping("/{id}/status")
    public Result<?> updateShopStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantShopService.updateShopStatus(merchantId, operatorId, id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 删除门店
     *
     * @param id 门店ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteShop(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantShopService.deleteShop(merchantId, operatorId, id);
        return Result.success("删除成功");
    }

    /**
     * 获取门店统计数据
     *
     * @param id 门店ID
     * @return 统计数据
     */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getShopStats(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> stats = merchantShopService.getShopStats(merchantId, id);
        return Result.success(stats);
    }
}
