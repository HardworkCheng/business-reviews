package com.businessreviews.service.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.ShopDetailVO;
import com.businessreviews.model.vo.ShopItemVO;
import java.util.Map;

/**
 * 商家门店服务接口
 * 提供商家运营中心的门店管理功能
 */
public interface MerchantShopService {
    
    /**
     * 获取门店列表
     */
    PageResult<ShopItemVO> getShopList(Long merchantId, Integer pageNum, Integer pageSize, Integer status, String keyword);
    
    /**
     * 获取门店详情
     */
    ShopDetailVO getShopDetail(Long merchantId, Long shopId);
    
    /**
     * 创建门店
     */
    Long createShop(Long merchantId, Long operatorId, Map<String, Object> request);
    
    /**
     * 更新门店
     */
    void updateShop(Long merchantId, Long operatorId, Long shopId, Map<String, Object> request);
    
    /**
     * 更新门店状态
     */
    void updateShopStatus(Long merchantId, Long operatorId, Long shopId, Integer status);
    
    /**
     * 删除门店
     */
    void deleteShop(Long merchantId, Long operatorId, Long shopId);
    
    /**
     * 获取门店统计数据
     */
    Map<String, Object> getShopStats(Long merchantId, Long shopId);
}
