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

/**
 * 通用服务实现类
 * <p>
 * 提供全局基础数据服务（如类目、话题）、搜索辅助以及地理位置解析服务。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final CategoryMapper categoryMapper;
    private final TopicMapper topicMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final RedisUtil redisUtil;

    /**
     * 获取所有分类列表
     * <p>
     * 查询系统中定义的所有分类，包括已禁用和未启用的分类。
     * 按排序字段(sort_order)升序排列。
     * </p>
     *
     * @return 所有分类的VO列表
     */
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

    /**
     * 获取启用分类列表
     * <p>
     * 仅查询当前状态为启用(status=1)的分类，适用于前端展示。
     * 按排序字段(sort_order)升序排列。
     * </p>
     *
     * @return 启用分类的VO列表
     */
    @Override
    public List<CategoryVO> getCategories() {
        // 查询启用的类目（status=1），按sort_order升序排序
        LambdaQueryWrapper<CategoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryDO::getStatus, 1)
                .orderByAsc(CategoryDO::getSortOrder);
        List<CategoryDO> categories = categoryMapper.selectList(wrapper);

        return categories.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取分类详情
     *
     * @param categoryId 分类ID
     * @return 分类详情VO
     * @throws BusinessException 如果分类不存在(40402)
     */
    @Override
    public CategoryVO getCategoryDetail(Long categoryId) {
        CategoryDO category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(40402, "分类不存在");
        }
        return convertToCategoryVO(category);
    }

    /**
     * 获取指定分类下的话题列表
     * <p>
     * 查询指定分类下状态为启用(status=1)的话题。
     * 按浏览量(view_count)降序排列，热门话题优先展示。
     * </p>
     *
     * @param categoryId 分类ID
     * @return 话题VO列表
     */
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

    /**
     * 获取全站热门话题
     * <p>
     * 查询全站标记为热门(hot=1)且启用(status=1)的话题。
     * 按浏览量(view_count)降序排列。
     * </p>
     *
     * @param limit 返回数量限制，默认10条
     * @return 热门话题VO列表
     */
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

    /**
     * 获取搜索建议
     * <p>
     * 根据用户输入的关键词前缀，从搜索历史中查找匹配的高频搜索词。
     * 取搜索次数最多的前10条作为建议。
     * </p>
     *
     * @param keyword 用户输入的关键词
     * @return 建议搜索词列表
     */
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

    /**
     * 获取热门搜索词
     * <p>
     * 获取全站搜索频率最高的关键词，通常取前20条。
     * </p>
     *
     * @return 热门搜索词列表
     */
    @Override
    public List<String> getHotSearches() {
        // 查询热门搜索
        List<String> hotSearches = searchHistoryMapper.selectHotKeywords(20);
        return hotSearches;
    }

    private CategoryVO convertToCategoryVO(CategoryDO category) {
        CategoryVO response = new CategoryVO();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setIcon(category.getIcon());
        response.setColor(category.getColor());
        response.setSortOrder(category.getSortOrder());
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

    // 高德地图Web服务Key (建议生产环境通过配置文件注入)
    private static final String AMAP_KEY = "1521141ae4ee08e1a0e37b59d2fd2438";

    /**
     * 根据IP地址获取城市名称
     * <p>
     * 调用高德地图Web服务API进行IP定位。
     * 如果IP为空或定位失败，默认返回"杭州市"。
     * </p>
     *
     * @param ip 客户端IP地址
     * @return 城市名称（如"杭州市"）或省份名称
     */
    @Override
    public String getCityByIp(String ip) {
        // 如果IP为空，高德会默认使用请求来源IP
        // 在后端请求时，如果未传ip参数，会返回后端服务器的地理位置
        try {
            String url = "https://restapi.amap.com/v3/ip?key=" + AMAP_KEY;
            if (ip != null && !ip.isEmpty()) {
                url += "&ip=" + ip;
            }
            log.info("Requesting Amap IP Location: {}", url);
            String response = cn.hutool.http.HttpUtil.get(url);
            log.info("Amap IP Response: {}", response);

            cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(response);
            if ("1".equals(json.getStr("status"))) {
                String city = json.getStr("city");
                if (cn.hutool.core.util.StrUtil.isNotBlank(city) && !cn.hutool.json.JSONUtil.isNull(json.get("city"))) {
                    // 有时候city是数组或对象，需要处理? 高德IP定位一般返回字符串
                    // 如果是空或者是[]，尝试用province
                    return city;
                }
                String province = json.getStr("province");
                if (cn.hutool.core.util.StrUtil.isNotBlank(province)) {
                    return province;
                }
            }
        } catch (Exception e) {
            log.error("Failed to get city by IP", e);
        }
        return "杭州市"; // 默认兜底
    }

    /**
     * 根据经纬度获取城市名称（逆地理编码）
     * <p>
     * 调用高德地图Web服务API进行逆地理编码解析。
     * 如果解析失败，默认返回"杭州市"。
     * </p>
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 城市名称（如"杭州市"）
     */
    @Override
    public String getCityByLocation(String longitude, String latitude) {
        try {
            String location = longitude + "," + latitude;
            String url = "https://restapi.amap.com/v3/geocode/regeo?key=" + AMAP_KEY +
                    "&location=" + location + "&extensions=base";
            log.info("Requesting Amap Regeo: {}", url);
            String response = cn.hutool.http.HttpUtil.get(url);
            // log.info("Amap Regeo Response: {}", response);

            cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(response);
            if ("1".equals(json.getStr("status"))) {
                cn.hutool.json.JSONObject regeocode = json.getJSONObject("regeocode");
                if (regeocode != null) {
                    cn.hutool.json.JSONObject addressComponent = regeocode.getJSONObject("addressComponent");
                    if (addressComponent != null) {
                        // 优先取city
                        Object cityObj = addressComponent.get("city");
                        if (cityObj instanceof String && cn.hutool.core.util.StrUtil.isNotBlank((String) cityObj)) {
                            return (String) cityObj;
                        }
                        // 如果city是空或者是[]，取province
                        return addressComponent.getStr("province");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get city by location", e);
        }
        return "杭州市"; // 默认兜底
    }
}
