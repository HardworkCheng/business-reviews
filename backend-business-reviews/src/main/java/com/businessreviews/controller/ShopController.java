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
     * 获取商家列表
     */
    @GetMapping
    public Result<PageResult<ShopItemResponse>> getShopList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ShopItemResponse> result = shopService.getShopList(categoryId, sortBy, pageNum, pageSize);
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
}
