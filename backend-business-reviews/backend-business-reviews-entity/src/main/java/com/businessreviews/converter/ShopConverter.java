package com.businessreviews.converter;

import com.businessreviews.model.dataobject.ShopDO;
import com.businessreviews.model.vo.ShopVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商家对象转换器
 * 负责ShopDO与ShopVO之间的转换
 * 
 * @author businessreviews
 */
@Component
public class ShopConverter {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * ShopDO转ShopVO
     * 
     * @param shopDO 商家数据对象
     * @return 商家视图对象
     */
    public ShopVO toVO(ShopDO shopDO) {
        if (shopDO == null) {
            return null;
        }
        
        ShopVO vo = new ShopVO();
        BeanUtils.copyProperties(shopDO, vo);
        
        // ID转字符串，避免前端精度丢失
        vo.setShopId(shopDO.getId() != null ? shopDO.getId().toString() : null);
        
        // 解析图片JSON为列表
        vo.setImages(parseImages(shopDO.getImages()));
        
        // 状态转文本
        vo.setStatusText(getStatusText(shopDO.getStatus()));
        
        return vo;
    }
    
    /**
     * ShopDO列表转ShopVO列表
     * 
     * @param doList 商家数据对象列表
     * @return 商家视图对象列表
     */
    public List<ShopVO> toVOList(List<ShopDO> doList) {
        if (doList == null) {
            return null;
        }
        return doList.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 解析图片JSON字符串为列表
     * 
     * @param imagesJson 图片JSON字符串
     * @return 图片URL列表
     */
    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取状态文本
     * 
     * @param status 状态码
     * @return 状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "营业中";
            case 2 -> "休息中";
            case 3 -> "已关闭";
            default -> "未知";
        };
    }
}
