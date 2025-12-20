package com.businessreviews.service.mobile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.ShopDetailVO;
import com.businessreviews.model.vo.ShopItemVO;
import com.businessreviews.model.dataobject.ShopDO;

import java.util.Map;

/**
 * 移动端店铺服务接口
 * 提供UniApp移动端的店铺浏览、搜索、收藏等功能
 */
public interface ShopService extends IService<ShopDO> {
    
    /**
     * 获取商家列表（支持分类筛选和关键词搜索）
     */
    PageResult<ShopItemVO> getShopList(Long categoryId, String keyword, String sortBy, Integer pageNum, Integer pageSize);
    
    /**
     * 获取附近商家
     */
    PageResult<ShopItemVO> getNearbyShops(Double latitude, Double longitude, Double distance, 
                                                 Long categoryId, Integer pageNum, Integer pageSize);
    
    /**
     * 搜索商家
     */
    PageResult<ShopItemVO> searchShops(String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 获取商家详情
     */
    ShopDetailVO getShopDetail(Long shopId, Long userId);
    
    /**
     * 获取商家笔记列表
     */
    PageResult<Object> getShopNotes(Long shopId, Integer pageNum, Integer pageSize);
    
    /**
     * 收藏商家
     */
    void bookmarkShop(Long userId, Long shopId);
    
    /**
     * 取消收藏商家
     */
    void unbookmarkShop(Long userId, Long shopId);
    
    /**
     * 检查是否已收藏商家
     */
    boolean isShopBookmarked(Long userId, Long shopId);
    
    /**
     * 获取商家评价列表
     */
    PageResult<Object> getShopReviews(Long shopId, Integer pageNum, Integer pageSize, String sortBy);
    
    /**
     * 发表商家评价
     */
    void postShopReview(Long userId, Long shopId, Map<String, Object> request);
    
    /**
     * 获取已注册商户列表（merchantId不为空的商户）
     * 用于笔记发布时关联商户
     */
    PageResult<ShopItemVO> getRegisteredShops(String keyword, Integer pageNum, Integer pageSize);
}
