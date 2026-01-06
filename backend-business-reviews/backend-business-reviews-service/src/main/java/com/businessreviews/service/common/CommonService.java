package com.businessreviews.service.common;

import com.businessreviews.model.vo.CategoryVO;
import com.businessreviews.model.vo.TopicVO;

import java.util.List;

/**
 * 通用业务服务接口
 * <p>
 * 核心职责：
 * 1. 提供全局基础数据（分类、话题）
 * 2. 提供搜索辅助功能（建议、热搜）
 * 3. 提供地理位置相关服务
 * </p>
 * 
 * @author businessreviews
 */
public interface CommonService {

    /**
     * 获取所有分类列表
     *
     * @return 全量分类列表
     */
    List<CategoryVO> getAllCategories();

    /**
     * 获取启用的类目列表（主要用于商家运营中心）
     * <p>
     * 过滤逻辑：只返回status=1的类目，并按sort_order升序排序
     * </p>
     * 
     * @return 启用的类目列表
     */
    List<CategoryVO> getCategories();

    /**
     * 获取分类详情
     *
     * @param categoryId 分类ID
     * @return 分类详情VO
     */
    CategoryVO getCategoryDetail(Long categoryId);

    /**
     * 获取指定分类下的话题列表
     *
     * @param categoryId 分类ID
     * @return 话题列表
     */
    List<TopicVO> getTopics(Long categoryId);

    /**
     * 获取全局热门话题
     *
     * @param limit 获取数量限制
     * @return 热门话题列表
     */
    List<TopicVO> getHotTopics(Integer limit);

    /**
     * 获取搜索建议
     * <p>
     * 根据关键词前缀匹配，返回相关性最高的建议词
     * </p>
     *
     * @param keyword 搜索关键词
     * @return 建议词列表
     */
    List<String> getSearchSuggestions(String keyword);

    /**
     * 获取热门搜索词
     * <p>
     * 基于搜索热度统计
     * </p>
     *
     * @return 热搜词列表
     */
    List<String> getHotSearches();

    /**
     * 根据IP地址获取城市信息
     *
     * @param ip 客户端IP地址
     * @return 城市名称
     */
    String getCityByIp(String ip);

    /**
     * 根据经纬度坐标获取城市信息
     * <p>
     * 调用地图服务进行逆地理编码
     * </p>
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 城市名称
     */
    String getCityByLocation(String longitude, String latitude);
}
