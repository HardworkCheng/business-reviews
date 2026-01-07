package com.businessreviews.service.impl.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.businessreviews.constants.RedisKeyConstants;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    /** 热门话题缓存TTL：30分钟 */
    private static final long HOT_TOPICS_TTL = 1800L;

    /** 热门搜索词缓存TTL：30分钟 */
    private static final long HOT_SEARCHES_TTL = 1800L;

    /** 启用分类列表缓存TTL：1小时 */
    private static final long CATEGORIES_TTL = 3600L;

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
    @SuppressWarnings("unchecked")
    public List<CategoryVO> getAllCategories() {
        // 尝试从缓存获取
        String cacheKey = "categories:all"; // 使用硬编码或RedisKeyConstants.CATEGORIES
        try {
            // 注意：List<CategoryVO> 是泛型，这里同样有类型擦除问题
            // 但对于简单的CategoryVO列表，Jackson通常能处理
            List<CategoryVO> cachedList = (List<CategoryVO>) redisUtil.getObject(cacheKey, List.class);
            if (cachedList != null && !cachedList.isEmpty()) {
                // 简单的检查，确保元素类型正确，或者依赖JSON序列化的兼容性
                // 在这里直接返回，如果前端不需要严格类型可能有隐患，但通常可行
                log.info("从缓存获取分类列表");
                // 由于类型擦除，这里得到的可能是List<LinkedHashMap>
                // 我们需要手动转换，或者使用TypeReference
                // 为了简单，我们使用 Hutool 或 Jackson 进行二次转换，或者修改 RedisUtil
                // 这里暂且假设 redisUtil.getObject 能返回正确的 JSON 结构，并即使是 LinkedHashMap 也能被前端接受（只要字段名对）
                // 但为了严谨，我们应该使用 TypeReference。鉴于 CommonServiceImpl 没有注入 ObjectMapper
                // 我们还是使用 redisUtil.get() + Hutool/Jackson 手动解析比较稳妥
                // 既然 getObject 已经返回了 List（虽然内容是Map），我们可以尝试转换
                // 最简单的通过 re-serialize 是低效的。

                // 重新做：
                return convertMapList(cachedList);
            }
        } catch (Exception e) {
            log.warn("分类列表缓存读取失败: {}", e.getMessage());
        }

        // 查询所有分类
        LambdaQueryWrapper<CategoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CategoryDO::getSortOrder);
        List<CategoryDO> categories = categoryMapper.selectList(wrapper);

        List<CategoryVO> list = categories.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());

        // 写入缓存 1小时
        try {
            redisUtil.setObject(cacheKey, list, 3600L);
            log.info("分类列表写入缓存");
        } catch (Exception e) {
            log.warn("分类列表缓存写入失败: {}", e.getMessage());
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    private List<CategoryVO> convertMapList(List<?> list) {
        if (list == null)
            return new ArrayList<>();
        // 如果第一个元素已经是 CategoryVO，直接返回
        if (!list.isEmpty() && list.get(0) instanceof CategoryVO) {
            return (List<CategoryVO>) list;
        }

        // 否则利用 JSON 转换
        String json = cn.hutool.json.JSONUtil.toJsonStr(list);
        return cn.hutool.json.JSONUtil.toList(json, CategoryVO.class);
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
    @SuppressWarnings("unchecked")
    public List<CategoryVO> getCategories() {
        // 尝试从缓存获取
        String cacheKey = RedisKeyConstants.CATEGORIES_ENABLED;
        try {
            List<CategoryVO> cachedList = (List<CategoryVO>) redisUtil.getObject(cacheKey, List.class);
            if (cachedList != null && !cachedList.isEmpty()) {
                log.debug("从缓存获取启用分类列表");
                return convertMapList(cachedList);
            }
        } catch (Exception e) {
            log.warn("启用分类缓存读取失败: {}", e.getMessage());
        }

        // 查询启用的类目（status=1），按sort_order升序排序
        LambdaQueryWrapper<CategoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryDO::getStatus, 1)
                .orderByAsc(CategoryDO::getSortOrder);
        List<CategoryDO> categories = categoryMapper.selectList(wrapper);

        List<CategoryVO> list = categories.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());

        // 写入缓存
        try {
            redisUtil.setObject(cacheKey, list, CATEGORIES_TTL);
            log.debug("启用分类列表写入缓存");
        } catch (Exception e) {
            log.warn("启用分类缓存写入失败: {}", e.getMessage());
        }

        return list;
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
        int actualLimit = limit != null ? limit : 10;
        String cacheKey = RedisKeyConstants.HOT_TOPICS_CACHE + ":" + actualLimit;

        // 尝试从缓存获取
        try {
            String json = redisUtil.get(cacheKey);
            if (json != null) {
                List<TopicVO> cached = objectMapper.readValue(json,
                        new TypeReference<List<TopicVO>>() {
                        });
                log.debug("从缓存获取热门话题: limit={}", actualLimit);
                return cached;
            }
        } catch (Exception e) {
            log.warn("热门话题缓存读取失败: {}", e.getMessage());
        }

        LambdaQueryWrapper<TopicDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TopicDO::getStatus, 1)
                .eq(TopicDO::getHot, 1)
                .orderByDesc(TopicDO::getViewCount)
                .last("LIMIT " + actualLimit);

        List<TopicDO> topics = topicMapper.selectList(wrapper);

        List<TopicVO> list = topics.stream()
                .map(this::convertToTopicVO)
                .collect(Collectors.toList());

        // 写入缓存
        try {
            redisUtil.set(cacheKey, objectMapper.writeValueAsString(list), HOT_TOPICS_TTL);
            log.debug("热门话题写入缓存: limit={}", actualLimit);
        } catch (Exception e) {
            log.warn("热门话题缓存写入失败: {}", e.getMessage());
        }

        return list;
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
        String cacheKey = RedisKeyConstants.HOT_SEARCHES_CACHE;

        // 尝试从缓存获取
        try {
            String json = redisUtil.get(cacheKey);
            if (json != null) {
                List<String> cached = objectMapper.readValue(json,
                        new TypeReference<List<String>>() {
                        });
                log.debug("从缓存获取热门搜索词");
                return cached;
            }
        } catch (Exception e) {
            log.warn("热门搜索词缓存读取失败: {}", e.getMessage());
        }

        // 查询热门搜索
        List<String> hotSearches = searchHistoryMapper.selectHotKeywords(20);

        // 写入缓存
        try {
            redisUtil.set(cacheKey, objectMapper.writeValueAsString(hotSearches), HOT_SEARCHES_TTL);
            log.debug("热门搜索词写入缓存");
        } catch (Exception e) {
            log.warn("热门搜索词缓存写入失败: {}", e.getMessage());
        }

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
