package com.businessreviews.web.app;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.model.vo.ShopDetailVO;
import com.businessreviews.model.vo.ShopItemVO;
import com.businessreviews.service.app.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 移动端店铺控制器 (UniApp)
 * <p>
 * 提供移动端用户的店铺相关API：
 * - GET /shops - 获取商家列表（支持分类筛选和关键词搜索）
 * - GET /shops/nearby - 获取附近商家
 * - GET /shops/search - 搜索商家
 * - GET /shops/{id} - 获取商家详情
 * - GET /shops/{id}/notes - 获取商家笔记列表
 * - GET /shops/{id}/reviews - 获取商家评价列表
 * - POST /shops/{id}/reviews - 发表商家评价
 * - POST /shops/{id}/bookmark - 收藏商家
 * - DELETE /shops/{id}/bookmark - 取消收藏商家
 * - GET /shops/registered - 获取已注册商户列表
 * </p>
 *
 * @author businessreviews
 * @see com.businessreviews.service.app.ShopService
 */
@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /**
     * 获取商家列表（支持分类筛选和关键词搜索）
     *
     * @param categoryId 分类ID
     * @param keyword    关键词
     * @param sortBy     排序方式
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 商家列表
     */
    @GetMapping
    public Result<PageResult<ShopItemVO>> getShopList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ShopItemVO> result = shopService.getShopList(categoryId, keyword, sortBy, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取附近商家
     */
    @GetMapping("/nearby")
    public Result<PageResult<ShopItemVO>> getNearbyShops(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double distance,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ShopItemVO> result = shopService.getNearbyShops(latitude, longitude, distance, categoryId, pageNum,
                pageSize);
        return Result.success(result);
    }

    /**
     * 搜索商家
     *
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 商家列表
     */
    @GetMapping("/search")
    public Result<PageResult<ShopItemVO>> searchShops(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ShopItemVO> result = shopService.searchShops(keyword, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取商家详情
     *
     * @param id 商家ID
     * @return 商家详情VO
     */
    @GetMapping("/{id}")
    public Result<ShopDetailVO> getShopDetail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        ShopDetailVO response = shopService.getShopDetail(id, userId);
        return Result.success(response);
    }

    /**
     * 获取商家笔记列表
     *
     * @param id       商家ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 笔记列表
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
     *
     * @param id 商家ID
     * @return 成功结果
     */
    @PostMapping("/{id}/bookmark")
    public Result<?> bookmarkShop(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        shopService.bookmarkShop(userId, id);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏商家
     *
     * @param id 商家ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}/bookmark")
    public Result<?> unbookmarkShop(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        shopService.unbookmarkShop(userId, id);
        return Result.success("取消收藏成功");
    }

    /**
     * 获取商家评价列表
     *
     * @param id       商家ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param sortBy   排序方式
     * @return 评价列表
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
     *
     * @param id      商家ID
     * @param request 评价内容
     * @return 成功结果
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
     * <p>
     * 用于笔记发布时关联商户。
     * </p>
     *
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 商户列表
     */
    @GetMapping("/registered")
    public Result<PageResult<ShopItemVO>> getRegisteredShops(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        PageResult<ShopItemVO> result = shopService.getRegisteredShops(keyword, pageNum, pageSize);
        return Result.success(result);
    }
}
