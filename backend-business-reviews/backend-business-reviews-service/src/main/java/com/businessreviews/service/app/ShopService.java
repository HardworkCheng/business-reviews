package com.businessreviews.service.app;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.ShopDetailVO;
import com.businessreviews.model.vo.ShopItemVO;
import com.businessreviews.model.dataobject.ShopDO;

import java.util.Map;

/**
 * 用户端店铺服务接口
 * <p>
 * 提供UniApp移动端的店铺浏览、搜索、收藏等功能
 * </p>
 * 
 * @author businessreviews
 */
public interface ShopService extends IService<ShopDO> {

    /**
     * 获取商家列表（支持分类筛选和关键词搜索）
     * 
     * @param categoryId 分类ID
     * @param keyword    关键词
     * @param sortBy     排序方式
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 商家列表分页数据
     */
    PageResult<ShopItemVO> getShopList(Long categoryId, String keyword, String sortBy, Integer pageNum,
            Integer pageSize);

    /**
     * 获取附近商家
     * 
     * @param latitude   纬度
     * @param longitude  经度
     * @param distance   距离（千米）
     * @param categoryId 分类ID
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 附近商家列表分页数据
     */
    PageResult<ShopItemVO> getNearbyShops(Double latitude, Double longitude, Double distance,
            Long categoryId, Integer pageNum, Integer pageSize);

    /**
     * 搜索商家
     * 
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 搜索结果商家列表分页数据
     */
    PageResult<ShopItemVO> searchShops(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取商家详情
     * 
     * @param shopId 商家ID
     * @param userId 当前用户ID（用于判断收藏状态）
     * @return 商家详情VO
     */
    ShopDetailVO getShopDetail(Long shopId, Long userId);

    /**
     * 获取商家笔记列表
     * 
     * @param shopId   商家ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 商家关联笔记列表分页数据
     */
    PageResult<Object> getShopNotes(Long shopId, Integer pageNum, Integer pageSize);

    /**
     * 收藏商家
     * 
     * @param userId 收藏人ID
     * @param shopId 商家ID
     */
    void bookmarkShop(Long userId, Long shopId);

    /**
     * 取消收藏商家
     * 
     * @param userId 操作人ID
     * @param shopId 商家ID
     */
    void unbookmarkShop(Long userId, Long shopId);

    /**
     * 检查是否已收藏商家
     * 
     * @param userId 用户ID
     * @param shopId 商家ID
     * @return true=已收藏，false=未收藏
     */
    boolean isShopBookmarked(Long userId, Long shopId);

    /**
     * 获取商家评价列表
     * 
     * @param shopId   商家ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param sortBy   排序方式
     * @return 商家评价列表分页数据
     */
    PageResult<Object> getShopReviews(Long shopId, Integer pageNum, Integer pageSize, String sortBy);

    /**
     * 发表商家评价
     * 
     * @param userId  评价人ID
     * @param shopId  商家ID
     * @param request 评价内容请求对象
     */
    void postShopReview(Long userId, Long shopId, Map<String, Object> request);

    /**
     * 获取已注册商户列表（merchantId不为空的商户）
     * 用于笔记发布时关联商户
     * 
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 已注册商户列表分页数据
     */
    PageResult<ShopItemVO> getRegisteredShops(String keyword, Integer pageNum, Integer pageSize);
}
