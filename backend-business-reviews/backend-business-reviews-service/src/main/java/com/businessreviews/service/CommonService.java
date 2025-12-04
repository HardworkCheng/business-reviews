package com.businessreviews.service;

import com.businessreviews.dto.response.CategoryResponse;
import com.businessreviews.dto.response.TopicResponse;

import java.util.List;

public interface CommonService {
    
    /**
     * 获取所有分类
     */
    List<CategoryResponse> getAllCategories();
    
    /**
     * 获取分类详情
     */
    CategoryResponse getCategoryDetail(Long categoryId);
    
    /**
     * 获取话题列表
     */
    List<TopicResponse> getTopics(Long categoryId);
    
    /**
     * 获取热门话题
     */
    List<TopicResponse> getHotTopics(Integer limit);
    
    /**
     * 搜索建议
     */
    List<String> getSearchSuggestions(String keyword);
    
    /**
     * 热门搜索
     */
    List<String> getHotSearches();
}
