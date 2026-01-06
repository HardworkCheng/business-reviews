package com.businessreviews.service.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.ShopDetailVO;
import com.businessreviews.model.vo.ShopItemVO;
import java.util.Map;

/**
 * 商家门店服务接口
 * <p>
 * 提供商家运营中心的门店管理功能
 * </p>
 * 
 * @author businessreviews
 */
public interface MerchantShopService {

    /**
     * 获取门店列表
     * 
     * @param merchantId 商家ID
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param status     状态过滤
     * @param keyword    关键词过滤
     * @return 门店列表分页数据
     */
    PageResult<ShopItemVO> getShopList(Long merchantId, Integer pageNum, Integer pageSize, Integer status,
            String keyword);

    /**
     * 获取门店详情
     * 
     * @param merchantId 商家ID
     * @param shopId     门店ID
     * @return 门店详情VO
     */
    ShopDetailVO getShopDetail(Long merchantId, Long shopId);

    /**
     * 创建门店
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param request    创建请求参数MAP
     * @return 新增门店ID
     */
    Long createShop(Long merchantId, Long operatorId, Map<String, Object> request);

    /**
     * 更新门店
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param shopId     门店ID
     * @param request    更新请求参数MAP
     */
    void updateShop(Long merchantId, Long operatorId, Long shopId, Map<String, Object> request);

    /**
     * 更新门店状态
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param shopId     门店ID
     * @param status     新状态
     */
    void updateShopStatus(Long merchantId, Long operatorId, Long shopId, Integer status);

    /**
     * 删除门店
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param shopId     门店ID
     */
    void deleteShop(Long merchantId, Long operatorId, Long shopId);

    /**
     * 获取门店统计数据
     * 
     * @param merchantId 商家ID
     * @param shopId     门店ID
     * @return 统计数据MAP
     */
    Map<String, Object> getShopStats(Long merchantId, Long shopId);
}
