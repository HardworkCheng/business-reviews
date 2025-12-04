package com.businessreviews.controller;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.dto.response.CategoryResponse;


import com.businessreviews.dto.response.NoteItemResponse;
import com.businessreviews.dto.response.ShopItemResponse;
import com.businessreviews.dto.response.TopicResponse;
import com.businessreviews.service.CommonService;
import com.businessreviews.service.NoteService;
import com.businessreviews.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;
    private final NoteService noteService;
    private final ShopService shopService;

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> list = commonService.getAllCategories();
        return Result.success(list);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/categories/{id}")
    public Result<CategoryResponse> getCategoryDetail(@PathVariable Long id) {
        CategoryResponse response = commonService.getCategoryDetail(id);
        return Result.success(response);
    }

    /**
     * 获取话题列表
     */
    @GetMapping("/topics")
    public Result<List<TopicResponse>> getTopics(@RequestParam(required = false) Long categoryId) {
        List<TopicResponse> list = commonService.getTopics(categoryId);
        return Result.success(list);
    }

    /**
     * 获取热门话题
     */
    @GetMapping("/topics/hot")
    public Result<List<TopicResponse>> getHotTopics(@RequestParam(defaultValue = "10") Integer limit) {
        List<TopicResponse> list = commonService.getHotTopics(limit);
        return Result.success(list);
    }

    /**
     * 综合搜索
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Map<String, Object> result = new HashMap<>();
        
        if ("all".equals(type) || "note".equals(type)) {
            PageResult<NoteItemResponse> notes = noteService.searchNotes(keyword, pageNum, pageSize);
            result.put("notes", notes);
        }
        
        if ("all".equals(type) || "shop".equals(type)) {
            PageResult<ShopItemResponse> shops = shopService.searchShops(keyword, pageNum, pageSize);
            result.put("shops", shops);
        }
        
        return Result.success(result);
    }

    /**
     * 搜索建议
     */
    @GetMapping("/search/suggestions")
    public Result<List<String>> getSearchSuggestions(@RequestParam String keyword) {
        List<String> suggestions = commonService.getSearchSuggestions(keyword);
        return Result.success(suggestions);
    }

    /**
     * 热门搜索
     */
    @GetMapping("/search/hot")
    public Result<List<String>> getHotSearches() {
        List<String> hotSearches = commonService.getHotSearches();
        return Result.success(hotSearches);
    }
}
