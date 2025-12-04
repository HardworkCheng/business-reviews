package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.response.NoteItemResponse;
import com.businessreviews.dto.response.ShopDetailResponse;
import com.businessreviews.dto.response.ShopItemResponse;
import com.businessreviews.entity.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    private final ShopMapper shopMapper;
    private final CategoryMapper categoryMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserStatsMapper userStatsMapper;

    @Override
    public PageResult<ShopItemResponse> getShopList(Long categoryId, String sortBy, Integer pageNum, Integer pageSize) {
        Page<Shop> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getStatus, 1);
        
        if (categoryId != null) {
            wrapper.eq(Shop::getCategoryId, categoryId);
        }
        
        // 排序
        if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(Shop::getRating);
        } else if ("popular".equals(sortBy)) {
            wrapper.orderByDesc(Shop::getPopularity);
        } else if ("price_asc".equals(sortBy)) {
            wrapper.orderByAsc(Shop::getAveragePrice);
        } else if ("price_desc".equals(sortBy)) {
            wrapper.orderByDesc(Shop::getAveragePrice);
        } else {
            wrapper.orderByDesc(Shop::getRating).orderByDesc(Shop::getPopularity);
        }
        
        Page<Shop> shopPage = shopMapper.selectPage(page, wrapper);
        
        List<ShopItemResponse> list = shopPage.getRecords().stream()
                .map(this::convertToShopItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, shopPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<ShopItemResponse> getNearbyShops(Double latitude, Double longitude, Double distance,
                                                        Long categoryId, Integer pageNum, Integer pageSize) {
        // 简化实现，实际应使用地理位置查询
        Page<Shop> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getStatus, 1)
               .isNotNull(Shop::getLatitude)
               .isNotNull(Shop::getLongitude);
        
        if (categoryId != null) {
            wrapper.eq(Shop::getCategoryId, categoryId);
        }
        
        wrapper.orderByDesc(Shop::getRating);
        
        Page<Shop> shopPage = shopMapper.selectPage(page, wrapper);
        
        List<ShopItemResponse> list = shopPage.getRecords().stream()
                .map(shop -> {
                    ShopItemResponse item = convertToShopItem(shop);
                    // 计算距离（简化实现）
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
    public PageResult<ShopItemResponse> searchShops(String keyword, Integer pageNum, Integer pageSize) {
        Page<Shop> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getStatus, 1)
               .and(w -> w.like(Shop::getName, keyword)
                         .or()
                         .like(Shop::getAddress, keyword))
               .orderByDesc(Shop::getRating);
        
        Page<Shop> shopPage = shopMapper.selectPage(page, wrapper);
        
        List<ShopItemResponse> list = shopPage.getRecords().stream()
                .map(this::convertToShopItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, shopPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public ShopDetailResponse getShopDetail(Long shopId, Long userId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getStatus() != 1) {
            throw new BusinessException(40402, "商家不存在");
        }
        
        ShopDetailResponse response = new ShopDetailResponse();
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
        
        // 检查是否已收藏
        if (userId != null) {
            response.setIsFavorited(isShopBookmarked(userId, shopId));
        } else {
            response.setIsFavorited(false);
        }
        
        return response;
    }

    @Override
    public PageResult<Object> getShopNotes(Long shopId, Integer pageNum, Integer pageSize) {
        Page<Note> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getShopId, shopId)
               .eq(Note::getStatus, 1)
               .orderByDesc(Note::getCreatedAt);
        
        Page<Note> notePage = noteMapper.selectPage(page, wrapper);
        
        List<Object> list = notePage.getRecords().stream()
                .map(note -> {
                    NoteItemResponse item = new NoteItemResponse();
                    item.setId(note.getId().toString());
                    item.setImage(note.getCoverImage());
                    item.setTitle(note.getTitle());
                    item.setLikes(note.getLikeCount());
                    item.setViews(note.getViewCount());
                    item.setCreatedAt(note.getCreatedAt().toString());
                    
                    User author = userMapper.selectById(note.getUserId());
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
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getStatus() != 1) {
            throw new BusinessException(40402, "商家不存在");
        }
        
        // 检查是否已收藏
        if (isShopBookmarked(userId, shopId)) {
            throw new BusinessException(40001, "已收藏");
        }
        
        // 插入收藏记录
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setType(2); // 商家类型
        favorite.setTargetId(shopId);
        userFavoriteMapper.insert(favorite);
        
        // 更新商家收藏数
        shopMapper.incrementFavoriteCount(shopId);
        
        // 更新用户收藏数
        userStatsMapper.incrementFavoriteCount(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbookmarkShop(Long userId, Long shopId) {
        // 检查是否已收藏
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getType, 2)
               .eq(UserFavorite::getTargetId, shopId);
        UserFavorite favorite = userFavoriteMapper.selectOne(wrapper);
        
        if (favorite == null) {
            throw new BusinessException(40001, "未收藏");
        }
        
        // 删除收藏记录
        userFavoriteMapper.deleteById(favorite.getId());
        
        // 更新商家收藏数
        shopMapper.decrementFavoriteCount(shopId);
        
        // 更新用户收藏数
        userStatsMapper.decrementFavoriteCount(userId);
    }

    @Override
    public boolean isShopBookmarked(Long userId, Long shopId) {
        if (userId == null) return false;
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getType, 2)
               .eq(UserFavorite::getTargetId, shopId);
        return userFavoriteMapper.selectCount(wrapper) > 0;
    }

    private ShopItemResponse convertToShopItem(Shop shop) {
        ShopItemResponse item = new ShopItemResponse();
        item.setId(shop.getId().toString());
        item.setName(shop.getName());
        item.setImage(shop.getHeaderImage());
        item.setRating(shop.getRating());
        item.setAvgPrice(shop.getAveragePrice() != null ? shop.getAveragePrice().intValue() : null);
        item.setAddress(shop.getAddress());
        item.setNoteCount(shop.getReviewCount());
        
        // 查询分类
        if (shop.getCategoryId() != null) {
            Category category = categoryMapper.selectById(shop.getCategoryId());
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
        return Arrays.asList(images.split(","));
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 简化的距离计算（Haversine公式）
        double R = 6371; // 地球半径（公里）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
