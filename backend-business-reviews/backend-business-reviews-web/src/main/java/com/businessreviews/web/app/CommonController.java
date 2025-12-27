package com.businessreviews.web.app;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.model.vo.CategoryVO;
import com.businessreviews.model.vo.NoteItemVO;
import com.businessreviews.model.vo.ShopItemVO;
import com.businessreviews.model.vo.TopicVO;
import com.businessreviews.service.common.CommonService;
import com.businessreviews.service.app.NoteService;
import com.businessreviews.service.app.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共接口控制器
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class CommonController {

    private final CommonService commonService;
    private final NoteService noteService;
    private final ShopService shopService;

    /**
     * 获取所有分类（移动端使用）
     */
    @GetMapping("/categories")
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> list = commonService.getAllCategories();
        return Result.success(list);
    }

    /**
     * 获取启用的类目列表（商家运营中心使用）
     * 只返回status=1的类目，按sort_order升序排序
     * 
     * @return 类目列表
     */
    @GetMapping("/api/common/categories")
    public Result<List<CategoryVO>> getCategories() {
        List<CategoryVO> list = commonService.getCategories();
        return Result.success(list);
    }

    @GetMapping("/categories/{id}")
    public Result<CategoryVO> getCategoryDetail(@PathVariable Long id) {
        CategoryVO response = commonService.getCategoryDetail(id);
        return Result.success(response);
    }

    @GetMapping("/topics")
    public Result<List<TopicVO>> getTopics(@RequestParam(required = false) Long categoryId) {
        List<TopicVO> list = commonService.getTopics(categoryId);
        return Result.success(list);
    }

    @GetMapping("/topics/hot")
    public Result<List<TopicVO>> getHotTopics(@RequestParam(defaultValue = "10") Integer limit) {
        List<TopicVO> list = commonService.getHotTopics(limit);
        return Result.success(list);
    }

    @GetMapping("/search")
    public Result<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Map<String, Object> result = new HashMap<>();
        
        if ("all".equals(type) || "note".equals(type)) {
            PageResult<NoteItemVO> notes = noteService.searchNotes(keyword, pageNum, pageSize);
            result.put("notes", notes);
        }
        
        if ("all".equals(type) || "shop".equals(type)) {
            PageResult<ShopItemVO> shops = shopService.searchShops(keyword, pageNum, pageSize);
            result.put("shops", shops);
        }
        
        return Result.success(result);
    }

    @GetMapping("/search/suggestions")
    public Result<List<String>> getSearchSuggestions(@RequestParam String keyword) {
        List<String> suggestions = commonService.getSearchSuggestions(keyword);
        return Result.success(suggestions);
    }

    @GetMapping("/search/hot")
    public Result<List<String>> getHotSearches() {
        List<String> hotSearches = commonService.getHotSearches();
        return Result.success(hotSearches);
    }
}
