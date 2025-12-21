package com.businessreviews.service.impl.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.businessreviews.model.vo.CategoryVO;
import com.businessreviews.model.vo.TopicVO;
import com.businessreviews.model.dataobject.CategoryDO;
import com.businessreviews.model.dataobject.SearchHistoryDO;
import com.businessreviews.model.dataobject.TopicDO;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.CategoryMapper;
import com.businessreviews.mapper.SearchHistoryMapper;
import com.businessreviews.mapper.TopicMapper;
import com.businessreviews.service.common.CommonService;
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
    public List<CategoryVO> getAllCategories() {
        // 查询所有分类
        LambdaQueryWrapper<CategoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CategoryDO::getSortOrder);
        List<CategoryDO> categories = categoryMapper.selectList(wrapper);
        
        return categories.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryVO getCategoryDetail(Long categoryId) {
        CategoryDO category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(40402, "分类不存在");
        }
        return convertToCategoryVO(category);
    }

    @Override
    public List<TopicVO> getTopics(Long categoryId) {
        LambdaQueryWrapper<TopicDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TopicDO::getStatus, 1)
               .orderByDesc(TopicDO::getViewCount);
        
        List<TopicDO> topics = topicMapper.selectList(wrapper);
        
        return topics.stream()
                .map(this::convertToTopicVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopicVO> getHotTopics(Integer limit) {
        LambdaQueryWrapper<TopicDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TopicDO::getStatus, 1)
               .eq(TopicDO::getHot, 1)
               .orderByDesc(TopicDO::getViewCount)
               .last("LIMIT " + (limit != null ? limit : 10));
        
        List<TopicDO> topics = topicMapper.selectList(wrapper);
        
        return topics.stream()
                .map(this::convertToTopicVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSearchSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 简单实现：从搜索历史中查找匹配的关键词
        LambdaQueryWrapper<SearchHistoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SearchHistoryDO::getKeyword, keyword)
               .groupBy(SearchHistoryDO::getKeyword)
               .orderByDesc(SearchHistoryDO::getSearchCount)
               .last("LIMIT 10");
        
        List<SearchHistoryDO> histories = searchHistoryMapper.selectList(wrapper);
        
        return histories.stream()
                .map(SearchHistoryDO::getKeyword)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getHotSearches() {
        // 查询热门搜索
        List<String> hotSearches = searchHistoryMapper.selectHotKeywords(20);
        return hotSearches;
    }

    private CategoryVO convertToCategoryVO(CategoryDO category) {
        CategoryVO response = new CategoryVO();
        response.setId(category.getId().toString());
        response.setName(category.getName());
        response.setIcon(category.getIcon());
        response.setSort(category.getSortOrder());
        return response;
    }

    private TopicVO convertToTopicVO(TopicDO topic) {
        TopicVO response = new TopicVO();
        response.setId(topic.getId().toString());
        response.setName(topic.getName());
        response.setDescription(topic.getDescription());
        response.setCoverImage(topic.getCoverImage());
        response.setNoteCount(topic.getNoteCount());
        response.setViewCount(topic.getViewCount());
        response.setHot(topic.getHot() != null && topic.getHot() == 1);
        return response;
    }
}
