package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.response.ShopDetailResponse;
import com.businessreviews.dto.response.ShopItemResponse;
import com.businessreviews.entity.Merchant;
import com.businessreviews.entity.Note;
import com.businessreviews.entity.Shop;
import com.businessreviews.entity.ShopReview;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.MerchantMapper;
import com.businessreviews.mapper.NoteMapper;
import com.businessreviews.mapper.ShopMapper;
import com.businessreviews.mapper.ShopReviewMapper;
import com.businessreviews.service.MerchantShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商家门店服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantShopServiceImpl implements MerchantShopService {

    private final ShopMapper shopMapper;
    private final MerchantMapper merchantMapper;
    private final NoteMapper noteMapper;
    private final ShopReviewMapper shopReviewMapper;

    @Override
    public PageResult<ShopItemResponse> getShopList(Long merchantId, Integer pageNum, Integer pageSize, 
            Integer status, String keyword) {
        log.info("获取门店列表: merchantId={}, pageNum={}, pageSize={}, status={}, keyword={}", 
                merchantId, pageNum, pageSize, status, keyword);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 构建查询条件 - 根据商家关联的门店查询
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getMerchantId, merchantId);
        
        if (status != null) {
            wrapper.eq(Shop::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Shop::getName, keyword)
                    .or().like(Shop::getAddress, keyword));
        }
        wrapper.orderByDesc(Shop::getCreatedAt);
        
        // 分页查询
        Page<Shop> page = new Page<>(pageNum, pageSize);
        Page<Shop> shopPage = shopMapper.selectPage(page, wrapper);
        
        // 转换结果
        List<ShopItemResponse> list = shopPage.getRecords().stream()
                .map(this::convertToShopItemResponse)
                .collect(Collectors.toList());
        
        PageResult<ShopItemResponse> result = new PageResult<>();
        result.setList(list);
        result.setTotal(shopPage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }


    @Override
    public ShopDetailResponse getShopDetail(Long merchantId, Long shopId) {
        log.info("获取门店详情: merchantId={}, shopId={}", merchantId, shopId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 查询门店
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40404, "门店不存在");
        }
        
        return convertToShopDetailResponse(shop);
    }

    @Override
    @Transactional
    public Long createShop(Long merchantId, Long operatorId, Map<String, Object> request) {
        log.info("创建门店: merchantId={}, operatorId={}", merchantId, operatorId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 创建门店
        Shop shop = new Shop();
        shop.setMerchantId(merchantId); // 关联商家ID
        shop.setName((String) request.get("name"));
        shop.setAddress((String) request.get("address"));
        shop.setPhone((String) request.get("phone"));
        shop.setBusinessHours((String) request.get("openingHours"));
        shop.setDescription((String) request.get("description"));
        shop.setHeaderImage((String) request.get("avatar"));
        shop.setImages((String) request.get("cover"));
        
        // 设置默认值
        shop.setStatus(request.get("status") != null ? (Integer) request.get("status") : 1);
        shop.setRating(BigDecimal.valueOf(5.0));
        shop.setTasteScore(BigDecimal.valueOf(5.0));
        shop.setEnvironmentScore(BigDecimal.valueOf(5.0));
        shop.setServiceScore(BigDecimal.valueOf(5.0));
        shop.setReviewCount(0);
        shop.setPopularity(0);
        shop.setCreatedAt(LocalDateTime.now());
        shop.setUpdatedAt(LocalDateTime.now());
        
        // 处理位置信息
        if (request.get("latitude") != null) {
            shop.setLatitude(new BigDecimal(request.get("latitude").toString()));
        }
        if (request.get("longitude") != null) {
            shop.setLongitude(new BigDecimal(request.get("longitude").toString()));
        }
        if (request.get("province") != null) {
            shop.setProvince((String) request.get("province"));
        }
        if (request.get("city") != null) {
            shop.setCity((String) request.get("city"));
        }
        if (request.get("district") != null) {
            shop.setDistrict((String) request.get("district"));
        }
        if (request.get("categoryId") != null) {
            Object catId = request.get("categoryId");
            if (catId instanceof Integer) {
                shop.setCategoryId((Integer) catId);
            } else if (catId instanceof Number) {
                shop.setCategoryId(((Number) catId).intValue());
            }
        }
        if (request.get("averagePrice") != null) {
            shop.setAveragePrice(new BigDecimal(request.get("averagePrice").toString()));
        }
        
        shopMapper.insert(shop);
        log.info("门店创建成功: shopId={}", shop.getId());
        
        return shop.getId();
    }

    @Override
    @Transactional
    public void updateShop(Long merchantId, Long operatorId, Long shopId, Map<String, Object> request) {
        log.info("更新门店: merchantId={}, shopId={}", merchantId, shopId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 查询门店
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40404, "门店不存在");
        }
        
        // 更新门店信息
        if (request.get("name") != null) {
            shop.setName((String) request.get("name"));
        }
        if (request.get("address") != null) {
            shop.setAddress((String) request.get("address"));
        }
        if (request.get("phone") != null) {
            shop.setPhone((String) request.get("phone"));
        }
        if (request.get("openingHours") != null) {
            shop.setBusinessHours((String) request.get("openingHours"));
        }
        if (request.get("description") != null) {
            shop.setDescription((String) request.get("description"));
        }
        if (request.get("avatar") != null) {
            shop.setHeaderImage((String) request.get("avatar"));
        }
        if (request.get("cover") != null) {
            shop.setImages((String) request.get("cover"));
        }
        if (request.get("status") != null) {
            shop.setStatus((Integer) request.get("status"));
        }
        if (request.get("latitude") != null) {
            shop.setLatitude(new BigDecimal(request.get("latitude").toString()));
        }
        if (request.get("longitude") != null) {
            shop.setLongitude(new BigDecimal(request.get("longitude").toString()));
        }
        if (request.get("averagePrice") != null) {
            shop.setAveragePrice(new BigDecimal(request.get("averagePrice").toString()));
        }
        
        shop.setUpdatedAt(LocalDateTime.now());
        shopMapper.updateById(shop);
        log.info("门店更新成功: shopId={}", shopId);
    }

    @Override
    @Transactional
    public void updateShopStatus(Long merchantId, Long operatorId, Long shopId, Integer status) {
        log.info("更新门店状态: merchantId={}, shopId={}, status={}", merchantId, shopId, status);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 查询门店
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40404, "门店不存在");
        }
        
        // 验证门店归属
        if (!merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException(40300, "无权操作此门店");
        }
        
        // 更新状态
        shop.setStatus(status);
        shop.setUpdatedAt(LocalDateTime.now());
        shopMapper.updateById(shop);
        log.info("门店状态更新成功: shopId={}, status={}", shopId, status);
    }

    @Override
    @Transactional
    public void deleteShop(Long merchantId, Long operatorId, Long shopId) {
        log.info("删除门店: merchantId={}, shopId={}", merchantId, shopId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 查询门店
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40404, "门店不存在");
        }
        
        // 验证门店归属
        if (!merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException(40300, "无权删除此门店");
        }
        
        // 删除门店（物理删除，也可以改为逻辑删除）
        shopMapper.deleteById(shopId);
        log.info("门店删除成功: shopId={}", shopId);
    }

    @Override
    public Map<String, Object> getShopStats(Long merchantId, Long shopId) {
        log.info("获取门店统计: merchantId={}, shopId={}", merchantId, shopId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 查询门店
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40404, "门店不存在");
        }
        
        // 统计笔记数量
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getShopId, shopId);
        Long noteCount = noteMapper.selectCount(noteWrapper);
        
        // 统计评论数量
        LambdaQueryWrapper<ShopReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(ShopReview::getShopId, shopId);
        Long reviewCount = shopReviewMapper.selectCount(reviewWrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalViews", shop.getPopularity() != null ? shop.getPopularity() : 0);
        stats.put("totalNotes", noteCount);
        stats.put("totalReviews", reviewCount);
        stats.put("avgRating", shop.getRating() != null ? shop.getRating() : 0.0);
        stats.put("tasteScore", shop.getTasteScore() != null ? shop.getTasteScore() : 0.0);
        stats.put("environmentScore", shop.getEnvironmentScore() != null ? shop.getEnvironmentScore() : 0.0);
        stats.put("serviceScore", shop.getServiceScore() != null ? shop.getServiceScore() : 0.0);
        
        return stats;
    }
    
    /**
     * 验证商家是否存在
     */
    private void validateMerchant(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(40404, "商家不存在");
        }
        if (merchant.getStatus() != 1) {
            throw new BusinessException(40300, "商家账号已被禁用");
        }
    }
    
    /**
     * 转换为门店列表项响应
     */
    private ShopItemResponse convertToShopItemResponse(Shop shop) {
        ShopItemResponse response = new ShopItemResponse();
        response.setId(shop.getId().toString());
        response.setName(shop.getName());
        response.setImage(shop.getHeaderImage());
        response.setAddress(shop.getAddress());
        response.setRating(shop.getRating());
        response.setAvgPrice(shop.getAveragePrice() != null ? shop.getAveragePrice().intValue() : null);
        response.setPhone(shop.getPhone());
        response.setStatus(shop.getStatus());
        response.setBusinessHours(shop.getBusinessHours());
        response.setCreatedAt(shop.getCreatedAt());
        response.setUpdatedAt(shop.getUpdatedAt());
        
        // 统计笔记数量
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getShopId, shop.getId());
        Long noteCount = noteMapper.selectCount(noteWrapper);
        response.setNoteCount(noteCount.intValue());
        
        return response;
    }
    
    /**
     * 转换为门店详情响应
     */
    private ShopDetailResponse convertToShopDetailResponse(Shop shop) {
        ShopDetailResponse response = new ShopDetailResponse();
        response.setId(shop.getId());
        response.setName(shop.getName());
        response.setHeaderImage(shop.getHeaderImage());
        response.setDescription(shop.getDescription());
        response.setPhone(shop.getPhone());
        response.setAddress(shop.getAddress());
        response.setLatitude(shop.getLatitude());
        response.setLongitude(shop.getLongitude());
        response.setBusinessHours(shop.getBusinessHours());
        response.setAveragePrice(shop.getAveragePrice());
        response.setRating(shop.getRating());
        response.setTasteScore(shop.getTasteScore());
        response.setEnvironmentScore(shop.getEnvironmentScore());
        response.setServiceScore(shop.getServiceScore());
        response.setReviewCount(shop.getReviewCount());
        response.setIsFavorited(false); // 商家端不需要收藏状态
        
        // 处理图片列表
        if (shop.getImages() != null && !shop.getImages().isEmpty()) {
            try {
                // 假设images是JSON数组格式
                response.setImages(java.util.Arrays.asList(shop.getImages().split(",")));
            } catch (Exception e) {
                response.setImages(new ArrayList<>());
            }
        }
        
        return response;
    }
}
