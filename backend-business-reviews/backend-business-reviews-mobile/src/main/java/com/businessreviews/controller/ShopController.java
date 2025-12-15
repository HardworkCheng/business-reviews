package com.businessreviews.controller;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.dto.response.ShopDetailResponse;
import com.businessreviews.dto.response.ShopItemResponse;
import com.businessreviews.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /**
     * 获取商家列表（支持分类筛选和关键词搜索）
     */
    @GetMapping
    public Result<PageResult<ShopItemResponse>> getShopList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ShopItemResponse> result = shopService.getShopList(categoryId, keyword, sortBy, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取附近商家
     */
    @GetMapping("/nearby")
    public Result<PageResult<ShopItemResponse>> getNearbyShops(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double distance,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ShopItemResponse> result = shopService.getNearbyShops(latitude, longitude, distance, categoryId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 搜索商家
     */
    @GetMapping("/search")
    public Result<PageResult<ShopItemResponse>> searchShops(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ShopItemResponse> result = shopService.searchShops(keyword, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取商家详情
     */
    @GetMapping("/{id}")
    public Result<ShopDetailResponse> getShopDetail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        ShopDetailResponse response = shopService.getShopDetail(id, userId);
        return Result.success(response);
    }

    /**
     * 获取商家笔记列表
     */
    @GetMapping("/{id}/notes")
    public Result<PageResult<Object>> getShopNotes(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<Object> result = shopService.getShopNotes(id, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 收藏商家
     */
    @PostMapping("/{id}/bookmark")
    public Result<?> bookmarkShop(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        shopService.bookmarkShop(userId, id);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏商家
     */
    @DeleteMapping("/{id}/bookmark")
    public Result<?> unbookmarkShop(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        shopService.unbookmarkShop(userId, id);
        return Result.success("取消收藏成功");
    }

    /**
     * 获取商家评价列表
     */
    @GetMapping("/{id}/reviews")
    public Result<PageResult<Object>> getShopReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "latest") String sortBy) {
        PageResult<Object> result = shopService.getShopReviews(id, pageNum, pageSize, sortBy);
        return Result.success(result);
    }

    /**
     * 发表商家评价
     */
    @PostMapping("/{id}/reviews")
    public Result<?> postShopReview(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> request) {
        Long userId = UserContext.requireUserId();
        shopService.postShopReview(userId, id, request);
        return Result.success("评价成功");
    }

    /**
     * 收藏商家（兼容旧接口）
     */
    @PostMapping("/{id}/favorite")
    public Result<?> favoriteShop(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        shopService.bookmarkShop(userId, id);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏商家（兼容旧接口）
     */
    @DeleteMapping("/{id}/favorite")
    public Result<?> unfavoriteShop(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        shopService.unbookmarkShop(userId, id);
        return Result.success("取消收藏成功");
    }

    /**
     * 获取已注册商户列表（merchantId不为空的商户）
     * 用于笔记发布时关联商户
     */
    @GetMapping("/registered")
    public Result<PageResult<ShopItemResponse>> getRegisteredShops(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        PageResult<ShopItemResponse> result = shopService.getRegisteredShops(keyword, pageNum, pageSize);
        return Result.success(result);
    }
}
