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
import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * IP定位获取城市
     */
    @GetMapping("/common/location/ip")
    public Result<Map<String, String>> getCityByIp(HttpServletRequest request) {
        // 尝试获取客户端IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 如果是本地IP，则不传给高德，让高德自动检测请求来源IP(服务器IP)
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            ip = null;
        }

        String city = commonService.getCityByIp(ip);

        Map<String, String> data = new HashMap<>();
        data.put("city", city); // 返回城市名称
        // 为了兼容前端现有逻辑，这里返回status=1
        data.put("status", "1");

        return Result.success(data);
    }

    /**
     * 逆地理编码（经纬度转城市）
     */
    @GetMapping("/common/location/regeo")
    public Result<Map<String, String>> getCityByLocation(@RequestParam String longitude,
            @RequestParam String latitude) {
        String city = commonService.getCityByLocation(longitude, latitude);

        Map<String, String> data = new HashMap<>();
        data.put("city", city);
        data.put("status", "1");

        return Result.success(data);
    }
}
