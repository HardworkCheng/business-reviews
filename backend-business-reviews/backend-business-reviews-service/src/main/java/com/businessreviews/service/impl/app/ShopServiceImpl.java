package com.businessreviews.service.impl.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.NoteItemVO;
import com.businessreviews.model.vo.ShopDetailVO;
import com.businessreviews.model.vo.ShopItemVO;
import com.businessreviews.model.dataobject.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.enums.ReviewStatus;
import com.businessreviews.service.app.ShopService;
import com.businessreviews.util.RedisUtil;
import com.businessreviews.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 店铺服务实现类
 * <p>
 * 处理店铺/商家的核心业务逻辑。
 * 核心功能包括：
 * 1. 店铺列表查询（推荐、附近、搜索、分类筛选）
 * 2. 店铺详情与评价查询
 * 3. 店铺收藏与取消
 * 4. 已入驻商家列表查询
 * <p>
 * 性能优化：使用 In-Memory Map Assembly 模式解决 N+1 查询问题。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, ShopDO> implements ShopService {

    private final ShopMapper shopMapper;
    private final CategoryMapper categoryMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserStatsMapper userStatsMapper;
    private final ShopReviewMapper shopReviewMapper;
    private final RedisUtil redisUtil;

    /**
     * 获取店铺列表（多条件筛选）
     * <p>
     * 支持按分类、关键词、排序方式进行综合查询。
     * 排序方式包括：评分、热度、价格升序/降序。
     * </p>
     *
     * @param categoryId 分类ID
     * @param keyword    搜索关键词
     * @param sortBy     排序方式 (rating/popular/price_asc/price_desc)
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 店铺VO分页列表
     */
    @Override
    public PageResult<ShopItemVO> getShopList(Long categoryId, String keyword, String sortBy, Integer pageNum,
            Integer pageSize) {
        Page<ShopDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopDO::getStatus, 1);

        if (categoryId != null) {
            wrapper.eq(ShopDO::getCategoryId, categoryId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(ShopDO::getName, keyword.trim())
                    .or()
                    .like(ShopDO::getAddress, keyword.trim()));
        }

        if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(ShopDO::getRating);
        } else if ("popular".equals(sortBy)) {
            wrapper.orderByDesc(ShopDO::getPopularity);
        } else if ("price_asc".equals(sortBy)) {
            wrapper.orderByAsc(ShopDO::getAveragePrice);
        } else if ("price_desc".equals(sortBy)) {
            wrapper.orderByDesc(ShopDO::getAveragePrice);
        } else {
            wrapper.orderByDesc(ShopDO::getRating).orderByDesc(ShopDO::getPopularity);
        }

        Page<ShopDO> shopPage = shopMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<ShopItemVO> list = convertShopList(shopPage.getRecords());

        return PageResult.of(list, shopPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取附近店铺
     * <p>
     * 基于经纬度查询附近的店铺，并计算距离。
     * 同时也支持按分类筛选。
     * </p>
     *
     * @param latitude   用户当前纬度
     * @param longitude  用户当前经度
     * @param distance   搜索半径(km)
     * @param categoryId 分类ID
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 店铺VO分页列表（包含距离信息）
     */
    @Override
    public PageResult<ShopItemVO> getNearbyShops(Double latitude, Double longitude, Double distance,
            Long categoryId, Integer pageNum, Integer pageSize) {

        // 使用 Redis GEO 查询附近商家（按距离升序）
        // 计算需要跳过的数量和获取的数量
        long offset = (long) (pageNum - 1) * pageSize;
        long limit = offset + pageSize + 50; // 多查一些以便过滤分类后仍有足够数据

        // 从 Redis GEO 获取附近商家ID和距离
        var geoResults = redisUtil.geoRadius(
                RedisKeyConstants.SHOP_GEO,
                longitude,
                latitude,
                distance != null ? distance : 10.0, // 默认10公里
                limit);

        if (geoResults.isEmpty()) {
            log.info("Redis GEO 未找到附近商家，降级查询数据库");
            return fallbackNearbyShops(latitude, longitude, distance, categoryId, pageNum, pageSize);
        }

        // 提取商家ID列表
        List<Long> shopIds = geoResults.stream()
                .map(result -> Long.parseLong(result.getContent().getName()))
                .collect(Collectors.toList());

        // 批量查询商家详情
        List<ShopDO> shops = shopMapper.selectBatchIds(shopIds);

        // 构建 shopId -> ShopDO 映射
        Map<Long, ShopDO> shopMap = shops.stream()
                .collect(Collectors.toMap(ShopDO::getId, s -> s));

        // 构建 shopId -> 距离 映射
        Map<Long, Double> distanceMap = new HashMap<>();
        for (var result : geoResults) {
            Long shopId = Long.parseLong(result.getContent().getName());
            if (result.getDistance() != null) {
                distanceMap.put(shopId, result.getDistance().getValue());
            }
        }

        // 批量预加载分类信息
        Map<Integer, CategoryDO> categoryMap = batchLoadCategories(shops);

        // 按 Redis GEO 返回的顺序（距离升序）转换并过滤
        List<ShopItemVO> allItems = new ArrayList<>();
        for (Long shopId : shopIds) {
            ShopDO shop = shopMap.get(shopId);
            if (shop == null || shop.getStatus() != 1) {
                continue; // 跳过不存在或已下架的商家
            }

            // 按分类过滤
            if (categoryId != null && !categoryId.equals(shop.getCategoryId().longValue())) {
                continue;
            }

            ShopItemVO item = convertToShopItem(shop, categoryMap);
            Double dist = distanceMap.get(shopId);
            if (dist != null) {
                item.setDistance(String.format("%.1fkm", dist));
            }
            allItems.add(item);
        }

        // 手动分页
        long total = allItems.size();
        int fromIndex = (int) Math.min(offset, allItems.size());
        int toIndex = (int) Math.min(offset + pageSize, allItems.size());
        List<ShopItemVO> pageItems = allItems.subList(fromIndex, toIndex);

        log.info("Redis GEO 附近商家查询: lat={}, lng={}, distance={}km, found={}",
                latitude, longitude, distance, total);

        return PageResult.of(pageItems, total, pageNum, pageSize);
    }

    /**
     * 降级方案：当 Redis GEO 不可用时，使用数据库查询
     */
    private PageResult<ShopItemVO> fallbackNearbyShops(Double latitude, Double longitude, Double distance,
            Long categoryId, Integer pageNum, Integer pageSize) {
        Page<ShopDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopDO::getStatus, 1)
                .isNotNull(ShopDO::getLatitude)
                .isNotNull(ShopDO::getLongitude);

        if (categoryId != null) {
            wrapper.eq(ShopDO::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(ShopDO::getRating);

        Page<ShopDO> shopPage = shopMapper.selectPage(page, wrapper);

        // 批量预加载分类信息，解决N+1问题
        Map<Integer, CategoryDO> categoryMap = batchLoadCategories(shopPage.getRecords());

        List<ShopItemVO> list = shopPage.getRecords().stream()
                .map(shop -> {
                    ShopItemVO item = convertToShopItem(shop, categoryMap);
                    if (shop.getLatitude() != null && shop.getLongitude() != null) {
                        double dist = calculateDistance(latitude, longitude,
                                shop.getLatitude().doubleValue(), shop.getLongitude().doubleValue());
                        item.setDistance(String.format("%.1fkm", dist));
                    }
                    return item;
                })
                .collect(Collectors.toList());

        return PageResult.of(list, shopPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 搜索店铺
     * <p>
     * 根据关键词模糊搜索店铺名称或地址。
     * 结果按评分降序排列。
     * </p>
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 店铺VO分页列表
     */
    @Override
    public PageResult<ShopItemVO> searchShops(String keyword, Integer pageNum, Integer pageSize) {
        Page<ShopDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopDO::getStatus, 1)
                .and(w -> w.like(ShopDO::getName, keyword)
                        .or()
                        .like(ShopDO::getAddress, keyword))
                .orderByDesc(ShopDO::getRating);

        Page<ShopDO> shopPage = shopMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<ShopItemVO> list = convertShopList(shopPage.getRecords());

        return PageResult.of(list, shopPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取店铺详情
     * <p>
     * 返回店铺的完整信息，包括评分明细、营业时间等。
     * 同时检查当前用户的收藏状态。
     * </p>
     *
     * @param shopId 店铺ID
     * @param userId 当前用户ID（可选）
     * @return 店铺详情VO
     * @throws BusinessException 如果店铺不存在或暂停营业(40402)
     */
    @Override
    public ShopDetailVO getShopDetail(Long shopId, Long userId) {
        // 先检查商家是否存在/正常，这些基础校验可以走缓存，但状态变更需要清理缓存
        // 尝试从缓存获取
        String cacheKey = RedisKeyConstants.SHOP_INFO + shopId;
        ShopDetailVO cachedShop = null;
        try {
            // 简单的VO对象可以使用 redisUtil.getObject
            cachedShop = redisUtil.getObject(cacheKey, ShopDetailVO.class);
        } catch (Exception e) {
            log.warn("店铺详情缓存读取失败: {}", e.getMessage());
        }

        if (cachedShop != null) {
            log.info("从缓存获取店铺详情: shopId={}", shopId);
            // 缓存中只存了基础信息，用户互动状态(是否点赞收藏)是动态的，需要单独设置
            return fillUserInteraction(cachedShop, shopId, userId);
        }

        ShopDO shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40402, "商家不存在");
        }
        if (shop.getStatus() != null && shop.getStatus() != 1) {
            throw new BusinessException(40402, "商家暂停营业");
        }

        ShopDetailVO response = new ShopDetailVO();
        response.setId(shop.getId());
        response.setName(shop.getName());
        response.setHeaderImage(shop.getHeaderImage());
        response.setImages(parseImages(shop.getImages()));
        response.setDescription(shop.getDescription());
        response.setRating(shop.getRating());
        response.setAveragePrice(shop.getAveragePrice());
        response.setAddress(shop.getAddress());
        response.setPhone(shop.getPhone());
        response.setBusinessHours(shop.getBusinessHours());
        response.setReviewCount(shop.getReviewCount());
        response.setTasteScore(shop.getTasteScore());
        response.setEnvironmentScore(shop.getEnvironmentScore());
        response.setServiceScore(shop.getServiceScore());
        response.setLatitude(shop.getLatitude());
        response.setLongitude(shop.getLongitude());

        // 写入缓存，设置过期时间30分钟
        try {
            redisUtil.setObject(cacheKey, response, 1800L);
            log.info("店铺详情写入缓存: shopId={}", shopId);
        } catch (Exception e) {
            log.warn("店铺详情缓存写入失败: {}", e.getMessage());
        }

        return fillUserInteraction(response, shopId, userId);
    }

    /**
     * 填充用户互动状态（点赞、收藏等） - 这部分不缓存
     */
    private ShopDetailVO fillUserInteraction(ShopDetailVO vo, Long shopId, Long userId) {
        if (userId != null) {
            vo.setFavorited(isShopBookmarked(userId, shopId));
        } else {
            vo.setFavorited(false);
        }
        return vo;
    }

    /**
     * 获取店铺关联的笔记列表
     * <p>
     * 查询在该店铺发布的或关联该店铺的所有笔记。
     * 包含批量预加载用户信息以优化性能。
     * </p>
     *
     * @param shopId   店铺ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 笔记列表
     */
    @Override
    public PageResult<Object> getShopNotes(Long shopId, Integer pageNum, Integer pageSize) {
        Page<NoteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteDO::getShopId, shopId)
                .eq(NoteDO::getStatus, 1)
                .orderByDesc(NoteDO::getCreatedAt);

        Page<NoteDO> notePage = noteMapper.selectPage(page, wrapper);
        List<NoteDO> notes = notePage.getRecords();

        // 批量预加载用户信息，解决N+1查询问题
        Set<Long> userIds = notes.stream()
                .map(NoteDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserDO> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<UserDO> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(UserDO::getId, Function.identity()));
        }

        final Map<Long, UserDO> finalUserMap = userMap;
        List<Object> list = notes.stream()
                .map(note -> {
                    NoteItemVO item = new NoteItemVO();
                    item.setId(note.getId().toString());
                    item.setImage(note.getCoverImage());
                    item.setTitle(note.getTitle());
                    item.setLikes(note.getLikeCount());
                    item.setViews(note.getViewCount());
                    item.setCreatedAt(note.getCreatedAt().toString());

                    // 从预加载的Map获取用户信息，O(1)复杂度
                    UserDO author = finalUserMap.get(note.getUserId());
                    if (author != null) {
                        item.setAuthor(author.getUsername());
                        item.setAuthorAvatar(author.getAvatar());
                        item.setAuthorId(author.getId().toString());
                    }
                    return (Object) item;
                })
                .collect(Collectors.toList());

        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    /**
     * 收藏店铺
     *
     * @param userId 用户ID
     * @param shopId 店铺ID
     * @throws BusinessException 如果店铺不存在(40402)或已收藏(40001)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookmarkShop(Long userId, Long shopId) {
        ShopDO shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getStatus() != 1) {
            throw new BusinessException(40402, "商家不存在");
        }

        if (isShopBookmarked(userId, shopId)) {
            throw new BusinessException(40001, "已收藏");
        }

        UserFavoriteDO favorite = new UserFavoriteDO();
        favorite.setUserId(userId);
        favorite.setType(2);
        favorite.setTargetId(shopId);
        userFavoriteMapper.insert(favorite);

        shopMapper.incrementFavoriteCount(shopId);
        userStatsMapper.incrementFavoriteCount(userId);
    }

    /**
     * 取消收藏店铺
     *
     * @param userId 用户ID
     * @param shopId 店铺ID
     * @throws BusinessException 如果未收藏(40001)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbookmarkShop(Long userId, Long shopId) {
        LambdaQueryWrapper<UserFavoriteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteDO::getUserId, userId)
                .eq(UserFavoriteDO::getType, 2)
                .eq(UserFavoriteDO::getTargetId, shopId);
        UserFavoriteDO favorite = userFavoriteMapper.selectOne(wrapper);

        if (favorite == null) {
            throw new BusinessException(40001, "未收藏");
        }

        userFavoriteMapper.deleteById(favorite.getId());
        shopMapper.decrementFavoriteCount(shopId);
        userStatsMapper.decrementFavoriteCount(userId);
    }

    @Override
    public boolean isShopBookmarked(Long userId, Long shopId) {
        if (userId == null)
            return false;
        LambdaQueryWrapper<UserFavoriteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteDO::getUserId, userId)
                .eq(UserFavoriteDO::getType, 2)
                .eq(UserFavoriteDO::getTargetId, shopId);
        return userFavoriteMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取店铺评价列表
     * <p>
     * 查询用户对店铺的直接评分和评价内容。
     * 支持按评分或时间排序。
     * </p>
     *
     * @param shopId   店铺ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param sortBy   排序方式 (rating/default)
     * @return 评价列表
     */
    @Override
    public PageResult<Object> getShopReviews(Long shopId, Integer pageNum, Integer pageSize, String sortBy) {
        Page<ShopReviewDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopReviewDO::getShopId, shopId)
                .eq(ShopReviewDO::getStatus, 1);

        if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(ShopReviewDO::getRating);
        } else {
            wrapper.orderByDesc(ShopReviewDO::getCreatedAt);
        }

        Page<ShopReviewDO> reviewPage = shopReviewMapper.selectPage(page, wrapper);
        List<ShopReviewDO> reviews = reviewPage.getRecords();

        // 批量预加载用户信息，解决N+1查询问题
        Set<Long> userIds = reviews.stream()
                .map(ShopReviewDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserDO> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<UserDO> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(UserDO::getId, Function.identity()));
        }

        final Map<Long, UserDO> finalUserMap = userMap;
        List<Object> list = reviews.stream()
                .map(review -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", review.getId());
                    item.put("rating", review.getRating());
                    item.put("tasteScore", review.getTasteScore());
                    item.put("environmentScore", review.getEnvironmentScore());
                    item.put("serviceScore", review.getServiceScore());
                    item.put("content", review.getContent());
                    item.put("createdAt", review.getCreatedAt() != null ? review.getCreatedAt().toString() : null);

                    // 从预加载的Map获取用户信息，O(1)复杂度
                    UserDO user = finalUserMap.get(review.getUserId());
                    if (user != null) {
                        item.put("username", user.getUsername());
                        item.put("userAvatar", user.getAvatar());
                    }

                    return (Object) item;
                })
                .collect(Collectors.toList());

        return PageResult.of(list, reviewPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 发表店铺评价
     * <p>
     * 用户提交对店铺的评分和评价内容。
     * 评价提交后会自动触发店铺综合评分的重新计算和更新。
     * </p>
     *
     * @param userId  用户ID
     * @param shopId  店铺ID
     * @param request 评价内容 (rating, tasteScore, etc.)
     * @throws BusinessException 如果店铺不存在(40402)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postShopReview(Long userId, Long shopId, Map<String, Object> request) {
        ShopDO shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getStatus() != 1) {
            throw new BusinessException(40402, "商家不存在");
        }

        ShopReviewDO review = new ShopReviewDO();
        review.setShopId(shopId);
        review.setUserId(userId);

        if (request.get("rating") != null) {
            review.setRating(new BigDecimal(request.get("rating").toString()));
        } else {
            review.setRating(BigDecimal.valueOf(5.0));
        }

        if (request.get("tasteScore") != null) {
            review.setTasteScore(new BigDecimal(request.get("tasteScore").toString()));
        }
        if (request.get("environmentScore") != null) {
            review.setEnvironmentScore(new BigDecimal(request.get("environmentScore").toString()));
        }
        if (request.get("serviceScore") != null) {
            review.setServiceScore(new BigDecimal(request.get("serviceScore").toString()));
        }

        review.setContent((String) request.get("content"));
        review.setStatus(ReviewStatus.NORMAL.getCode());
        review.setLikeCount(0);
        review.setCreatedAt(LocalDateTime.now());

        shopReviewMapper.insert(review);
        updateShopRating(shopId);

        log.info("用户{}对商家{}发表评价成功", userId, shopId);
    }

    private void updateShopRating(Long shopId) {
        LambdaQueryWrapper<ShopReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopReviewDO::getShopId, shopId)
                .eq(ShopReviewDO::getStatus, ReviewStatus.NORMAL.getCode());
        List<ShopReviewDO> reviews = shopReviewMapper.selectList(wrapper);

        if (!reviews.isEmpty()) {
            double avgRating = reviews.stream()
                    .mapToDouble(r -> r.getRating().doubleValue())
                    .average()
                    .orElse(5.0);
            double avgTaste = reviews.stream()
                    .filter(r -> r.getTasteScore() != null)
                    .mapToDouble(r -> r.getTasteScore().doubleValue())
                    .average()
                    .orElse(5.0);
            double avgEnvironment = reviews.stream()
                    .filter(r -> r.getEnvironmentScore() != null)
                    .mapToDouble(r -> r.getEnvironmentScore().doubleValue())
                    .average()
                    .orElse(5.0);
            double avgService = reviews.stream()
                    .filter(r -> r.getServiceScore() != null)
                    .mapToDouble(r -> r.getServiceScore().doubleValue())
                    .average()
                    .orElse(5.0);

            ShopDO shop = shopMapper.selectById(shopId);
            shop.setRating(BigDecimal.valueOf(avgRating));
            shop.setTasteScore(BigDecimal.valueOf(avgTaste));
            shop.setEnvironmentScore(BigDecimal.valueOf(avgEnvironment));
            shop.setServiceScore(BigDecimal.valueOf(avgService));
            shop.setReviewCount(reviews.size());
            shopMapper.updateById(shop);
        }
    }

    /**
     * 批量转换店铺列表 - 解决N+1查询问题
     * 使用In-Memory Map预加载分类信息，将查询复杂度从O(N)降为O(1)
     * 
     * @param shops 店铺列表
     * @return 转换后的VO列表
     */
    private List<ShopItemVO> convertShopList(List<ShopDO> shops) {
        if (shops == null || shops.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量预加载分类信息
        Map<Integer, CategoryDO> categoryMap = batchLoadCategories(shops);

        return shops.stream()
                .map(shop -> convertToShopItem(shop, categoryMap))
                .collect(Collectors.toList());
    }

    /**
     * 批量加载分类信息
     * 
     * @param shops 店铺列表
     * @return 分类Map
     */
    private Map<Integer, CategoryDO> batchLoadCategories(List<ShopDO> shops) {
        Set<Integer> categoryIds = shops.stream()
                .map(ShopDO::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<CategoryDO> categories = categoryMapper.selectBatchIds(categoryIds);
        return categories.stream()
                .collect(Collectors.toMap(CategoryDO::getId, Function.identity()));
    }

    /**
     * 转换单个店铺 - 使用预加载的Map获取分类数据
     * 
     * @param shop        店铺实体
     * @param categoryMap 预加载的分类Map
     * @return 店铺VO
     */
    private ShopItemVO convertToShopItem(ShopDO shop, Map<Integer, CategoryDO> categoryMap) {
        ShopItemVO item = new ShopItemVO();
        item.setId(shop.getId().toString());
        item.setName(shop.getName());
        item.setImage(shop.getHeaderImage());
        item.setRating(shop.getRating());
        item.setAvgPrice(shop.getAveragePrice() != null ? shop.getAveragePrice().intValue() : null);
        item.setAddress(shop.getAddress());
        item.setNoteCount(shop.getReviewCount());

        // 从预加载的Map获取分类信息，O(1)复杂度
        if (shop.getCategoryId() != null) {
            CategoryDO category = categoryMap.get(shop.getCategoryId());
            if (category != null) {
                item.setCategory(category.getName());
            }
        }

        return item;
    }

    private List<String> parseImages(String images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        if (images.trim().startsWith("[") && images.trim().endsWith("]")) {
            try {
                String content = images.trim().substring(1, images.trim().length() - 1);
                if (content.isEmpty()) {
                    return List.of();
                }
                return Arrays.stream(content.split(","))
                        .map(s -> s.trim().replaceAll("^\"|\"$", ""))
                        .filter(s -> !s.isEmpty())
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception e) {
                log.warn("解析图片JSON数组失败，使用逗号分割: {}", images, e);
            }
        }

        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 获取已入驻商家列表
     * <p>
     * 仅返回已绑定商家账号(merchantId不为空)的店铺。
     * </p>
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 店铺VO分页列表
     */
    @Override
    public PageResult<ShopItemVO> getRegisteredShops(String keyword, Integer pageNum, Integer pageSize) {
        Page<ShopDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopDO> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(ShopDO::getStatus, 1)
                .isNotNull(ShopDO::getMerchantId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(ShopDO::getName, keyword.trim())
                    .or()
                    .like(ShopDO::getAddress, keyword.trim()));
        }

        wrapper.orderByDesc(ShopDO::getRating).orderByDesc(ShopDO::getPopularity);

        Page<ShopDO> shopPage = shopMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<ShopItemVO> list = convertShopList(shopPage.getRecords());

        return PageResult.of(list, shopPage.getTotal(), pageNum, pageSize);
    }
}
