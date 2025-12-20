package com.businessreviews.service.common;

import com.businessreviews.model.vo.CategoryVO;
import com.businessreviews.model.vo.TopicVO;

import java.util.List;

/**
 * 通用业务服务接口
 * 提供分类、话题、搜索等公共功能，供Mobile端和Merchant端共用
 */
public interface CommonService {
    
    /**
     * 获取所有分类
     */
    List<CategoryVO> getAllCategories();
    
    /**
     * 获取分类详情
     */
    CategoryVO getCategoryDetail(Long categoryId);
    
    /**
     * 获取话题列表
     */
    List<TopicVO> getTopics(Long categoryId);
    
    /**
     * 获取热门话题
     */
    List<TopicVO> getHotTopics(Integer limit);
    
    /**
     * 搜索建议
     */
    List<String> getSearchSuggestions(String keyword);
    
    /**
     * 热门搜索
     */
    List<String> getHotSearches();
}
