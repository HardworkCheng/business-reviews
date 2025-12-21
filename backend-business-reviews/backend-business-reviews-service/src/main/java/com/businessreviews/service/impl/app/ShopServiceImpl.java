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
import com.businessreviews.service.app.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public PageResult<ShopItemVO> getShopList(Long categoryId, String keyword, String sortBy, Integer pageNum, Integer pageSize) {
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
        
        List<ShopItemVO> list = shopPage.getRecords().stream()
                .map(this::convertToShopItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, shopPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<ShopItemVO> getNearbyShops(Double latitude, Double longitude, Double distance,
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
        
        List<ShopItemVO> list = shopPage.getRecords().stream()
                .map(shop -> {
                    ShopItemVO item = convertToShopItem(shop);
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
        
        List<ShopItemVO> list = shopPage.getRecords().stream()
                .map(this::convertToShopItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, shopPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public ShopDetailVO getShopDetail(Long shopId, Long userId) {
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
        
        if (userId != null) {
            response.setFavorited(isShopBookmarked(userId, shopId));
        } else {
            response.setFavorited(false);
        }
        
        return response;
    }

    @Override
    public PageResult<Object> getShopNotes(Long shopId, Integer pageNum, Integer pageSize) {
        Page<NoteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteDO::getShopId, shopId)
               .eq(NoteDO::getStatus, 1)
               .orderByDesc(NoteDO::getCreatedAt);
        
        Page<NoteDO> notePage = noteMapper.selectPage(page, wrapper);
        
        List<Object> list = notePage.getRecords().stream()
                .map(note -> {
                    NoteItemVO item = new NoteItemVO();
                    item.setId(note.getId().toString());
                    item.setImage(note.getCoverImage());
                    item.setTitle(note.getTitle());
                    item.setLikes(note.getLikeCount());
                    item.setViews(note.getViewCount());
                    item.setCreatedAt(note.getCreatedAt().toString());
                    
                    UserDO author = userMapper.selectById(note.getUserId());
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
        if (userId == null) return false;
        LambdaQueryWrapper<UserFavoriteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteDO::getUserId, userId)
               .eq(UserFavoriteDO::getType, 2)
               .eq(UserFavoriteDO::getTargetId, shopId);
        return userFavoriteMapper.selectCount(wrapper) > 0;
    }

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
        
        List<Object> list = reviewPage.getRecords().stream()
                .map(review -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", review.getId());
                    item.put("rating", review.getRating());
                    item.put("tasteScore", review.getTasteScore());
                    item.put("environmentScore", review.getEnvironmentScore());
                    item.put("serviceScore", review.getServiceScore());
                    item.put("content", review.getContent());
                    item.put("createdAt", review.getCreatedAt() != null ? review.getCreatedAt().toString() : null);
                    
                    UserDO user = userMapper.selectById(review.getUserId());
                    if (user != null) {
                        item.put("username", user.getUsername());
                        item.put("userAvatar", user.getAvatar());
                    }
                    
                    return (Object) item;
                })
                .collect(Collectors.toList());
        
        return PageResult.of(list, reviewPage.getTotal(), pageNum, pageSize);
    }

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
        review.setStatus(1);
        review.setLikeCount(0);
        review.setCreatedAt(LocalDateTime.now());
        
        shopReviewMapper.insert(review);
        updateShopRating(shopId);
        
        log.info("用户{}对商家{}发表评价成功", userId, shopId);
    }
    
    private void updateShopRating(Long shopId) {
        LambdaQueryWrapper<ShopReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopReviewDO::getShopId, shopId)
               .eq(ShopReviewDO::getStatus, 1);
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

    private ShopItemVO convertToShopItem(ShopDO shop) {
        ShopItemVO item = new ShopItemVO();
        item.setId(shop.getId().toString());
        item.setName(shop.getName());
        item.setImage(shop.getHeaderImage());
        item.setRating(shop.getRating());
        item.setAvgPrice(shop.getAveragePrice() != null ? shop.getAveragePrice().intValue() : null);
        item.setAddress(shop.getAddress());
        item.setNoteCount(shop.getReviewCount());
        
        if (shop.getCategoryId() != null) {
            CategoryDO category = categoryMapper.selectById(shop.getCategoryId());
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
        
        List<ShopItemVO> list = shopPage.getRecords().stream()
                .map(this::convertToShopItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, shopPage.getTotal(), pageNum, pageSize);
    }
}
