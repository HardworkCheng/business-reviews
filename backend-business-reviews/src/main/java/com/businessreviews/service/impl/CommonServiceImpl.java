package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.businessreviews.common.Constants;
import com.businessreviews.dto.response.CategoryResponse;
import com.businessreviews.dto.response.TopicResponse;
import com.businessreviews.entity.Category;
import com.businessreviews.entity.SearchHistory;
import com.businessreviews.entity.Topic;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.CategoryMapper;
import com.businessreviews.mapper.SearchHistoryMapper;
import com.businessreviews.mapper.TopicMapper;
import com.businessreviews.service.CommonService;
import com.businessreviews.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final CategoryMapper categoryMapper;
    private final TopicMapper topicMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final RedisUtil redisUtil;

    @Override
    public List<CategoryResponse> getAllCategories() {
        // 查询所有分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);
        
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryDetail(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(40402, "分类不存在");
        }
        return convertToCategoryResponse(category);
    }

    @Override
    public List<TopicResponse> getTopics(Long categoryId) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getStatus, 1)
               .orderByDesc(Topic::getViewCount);
        
        List<Topic> topics = topicMapper.selectList(wrapper);
        
        return topics.stream()
                .map(this::convertToTopicResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopicResponse> getHotTopics(Integer limit) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getStatus, 1)
               .eq(Topic::getIsHot, 1)
               .orderByDesc(Topic::getViewCount)
               .last("LIMIT " + (limit != null ? limit : 10));
        
        List<Topic> topics = topicMapper.selectList(wrapper);
        
        return topics.stream()
                .map(this::convertToTopicResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSearchSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 简单实现：从搜索历史中查找匹配的关键词
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SearchHistory::getKeyword, keyword)
               .groupBy(SearchHistory::getKeyword)
               .orderByDesc(SearchHistory::getSearchCount)
               .last("LIMIT 10");
        
        List<SearchHistory> histories = searchHistoryMapper.selectList(wrapper);
        
        return histories.stream()
                .map(SearchHistory::getKeyword)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getHotSearches() {
        // 查询热门搜索
        List<String> hotSearches = searchHistoryMapper.selectHotKeywords(20);
        return hotSearches;
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId().toString());
        response.setName(category.getName());
        response.setIcon(category.getIcon());
        response.setSort(category.getSortOrder());
        return response;
    }

    private TopicResponse convertToTopicResponse(Topic topic) {
        TopicResponse response = new TopicResponse();
        response.setId(topic.getId().toString());
        response.setName(topic.getName());
        response.setDescription(topic.getDescription());
        response.setCoverImage(topic.getCoverImage());
        response.setNoteCount(topic.getNoteCount());
        response.setViewCount(topic.getViewCount());
        response.setIsHot(topic.getIsHot() != null && topic.getIsHot() == 1);
        return response;
    }
}
