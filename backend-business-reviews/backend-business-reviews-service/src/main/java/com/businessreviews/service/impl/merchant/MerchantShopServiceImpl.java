package com.businessreviews.service.impl.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.ShopDetailVO;
import com.businessreviews.model.vo.ShopItemVO;
import com.businessreviews.model.dataobject.CategoryDO;
import com.businessreviews.model.dataobject.MerchantDO;
import com.businessreviews.model.dataobject.NoteDO;
import com.businessreviews.model.dataobject.ShopDO;
import com.businessreviews.model.dataobject.ShopReviewDO;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.CategoryMapper;
import com.businessreviews.mapper.MerchantMapper;
import com.businessreviews.mapper.NoteMapper;
import com.businessreviews.mapper.ShopMapper;
import com.businessreviews.mapper.ShopReviewMapper;
import com.businessreviews.service.merchant.MerchantShopService;
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
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<ShopItemVO> getShopList(Long merchantId, Integer pageNum, Integer pageSize,
            Integer status, String keyword) {
        log.info("获取门店列表: merchantId={}, pageNum={}, pageSize={}, status={}, keyword={}",
                merchantId, pageNum, pageSize, status, keyword);

        // 验证商家
        validateMerchant(merchantId);

        // 构建查询条件 - 根据商家关联的门店查询
        LambdaQueryWrapper<ShopDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopDO::getMerchantId, merchantId);

        // 检查是否有关联的店铺，如果没有则创建默认店铺
        long shopCount = shopMapper.selectCount(wrapper);
        if (shopCount == 0) {
            log.info("商家{}没有关联的店铺，创建默认店铺", merchantId);
            createDefaultShop(merchantId);
        }

        if (status != null) {
            wrapper.eq(ShopDO::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ShopDO::getName, keyword)
                    .or().like(ShopDO::getAddress, keyword));
        }
        wrapper.orderByDesc(ShopDO::getCreatedAt);

        // 分页查询
        Page<ShopDO> page = new Page<>(pageNum, pageSize);
        Page<ShopDO> shopPage = shopMapper.selectPage(page, wrapper);

        // 转换结果
        List<ShopItemVO> list = shopPage.getRecords().stream()
                .map(this::convertToShopItemVO)
                .collect(Collectors.toList());

        PageResult<ShopItemVO> result = new PageResult<>();
        result.setList(list);
        result.setTotal(shopPage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    /**
     * 为商家创建默认店铺
     */
    private void createDefaultShop(Long merchantId) {
        try {
            // 获取商家信息
            MerchantDO merchant = merchantMapper.selectById(merchantId);
            if (merchant == null) {
                log.error("商家不存在: merchantId={}", merchantId);
                return;
            }

            // 创建默认店铺
            ShopDO shop = new ShopDO();
            shop.setMerchantId(merchantId);
            shop.setName(merchant.getName() != null ? merchant.getName() : "我的店铺");
            shop.setDescription("欢迎来到我的店铺");
            shop.setStatus(1); // 营业中
            shop.setCategoryId(1); // 默认分类
            shop.setBusinessHours("09:00-22:00");
            shop.setRating(BigDecimal.valueOf(5.0));
            shop.setTasteScore(BigDecimal.valueOf(5.0));
            shop.setEnvironmentScore(BigDecimal.valueOf(5.0));
            shop.setServiceScore(BigDecimal.valueOf(5.0));
            shop.setReviewCount(0);
            shop.setPopularity(0);
            shop.setCreatedAt(LocalDateTime.now());
            shop.setUpdatedAt(LocalDateTime.now());

            shopMapper.insert(shop);
            log.info("为商家{}创建默认店铺成功: shopId={}", merchantId, shop.getId());
        } catch (Exception e) {
            log.error("为商家{}创建默认店铺失败", merchantId, e);
        }
    }

    @Override
    public ShopDetailVO getShopDetail(Long merchantId, Long shopId) {
        log.info("获取门店详情: merchantId={}, shopId={}", merchantId, shopId);

        // 验证商家
        validateMerchant(merchantId);

        // 查询门店
        ShopDO shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40404, "门店不存在");
        }

        return convertToShopDetailVO(shop);
    }

    @Override
    @Transactional
    public Long createShop(Long merchantId, Long operatorId, Map<String, Object> request) {
        log.info("创建门店: merchantId={}, operatorId={}", merchantId, operatorId);

        // 验证商家
        validateMerchant(merchantId);

        // 创建门店
        ShopDO shop = new ShopDO();
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
        if (request.get("categoryId") != null) {
            Object catId = request.get("categoryId");
            if (catId instanceof Integer) {
                shop.setCategoryId((Integer) catId);
            } else if (catId instanceof Number) {
                shop.setCategoryId(((Number) catId).intValue());
            }
            // 验证类目ID有效性
            validateCategoryId(shop.getCategoryId());
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
        ShopDO shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40404, "门店不存在");
        }

        // 验证商家权限 - 确保商家只能更新自己的店铺
        if (!merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException(40403, "无权限操作此门店");
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
        if (request.get("businessHours") != null) {
            shop.setBusinessHours((String) request.get("businessHours"));
        }
        if (request.get("description") != null) {
            shop.setDescription((String) request.get("description"));
        }
        if (request.get("headerImage") != null) {
            shop.setHeaderImage((String) request.get("headerImage"));
        }
        if (request.get("images") != null) {
            shop.setImages((String) request.get("images"));
        }
        if (request.get("status") != null) {
            shop.setStatus((Integer) request.get("status"));
        }
        if (request.get("categoryId") != null) {
            Integer categoryId = (Integer) request.get("categoryId");
            // 验证类目ID有效性
            validateCategoryId(categoryId);
            shop.setCategoryId(categoryId);
        }
        if (request.get("latitude") != null && !request.get("latitude").toString().trim().isEmpty()) {
            try {
                shop.setLatitude(new BigDecimal(request.get("latitude").toString()));
            } catch (NumberFormatException e) {
                log.warn("纬度格式错误: {}", request.get("latitude"));
            }
        }
        if (request.get("longitude") != null && !request.get("longitude").toString().trim().isEmpty()) {
            try {
                shop.setLongitude(new BigDecimal(request.get("longitude").toString()));
            } catch (NumberFormatException e) {
                log.warn("经度格式错误: {}", request.get("longitude"));
            }
        }
        if (request.get("averagePrice") != null && !request.get("averagePrice").toString().trim().isEmpty()) {
            try {
                shop.setAveragePrice(new BigDecimal(request.get("averagePrice").toString()));
            } catch (NumberFormatException e) {
                log.warn("人均消费格式错误: {}", request.get("averagePrice"));
            }
        }

        shop.setUpdatedAt(LocalDateTime.now());

        // 记录更新的字段
        log.info("更新门店字段: shopId={}, 更新内容={}", shopId, request.keySet());

        int updateResult = shopMapper.updateById(shop);
        if (updateResult > 0) {
            log.info("门店更新成功: shopId={}, 影响行数={}", shopId, updateResult);

            // 同步门店名称和联系电话到商家基本信息
            // 注意：这里假设商家只有一个主门店，或者用户希望同步修改商家信息
            if (request.containsKey("name") || request.containsKey("phone")) {
                MerchantDO merchantUpdate = new MerchantDO();
                merchantUpdate.setId(merchantId);
                boolean needSync = false;

                if (request.containsKey("name")) {
                    merchantUpdate.setName((String) request.get("name"));
                    needSync = true;
                }
                if (request.containsKey("phone")) {
                    merchantUpdate.setContactPhone((String) request.get("phone"));
                    needSync = true;
                }

                if (needSync) {
                    merchantMapper.updateById(merchantUpdate);
                    log.info("同步门店信息到商家成功: merchantId={}, shopId={}", merchantId, shopId);
                }
            }
        } else {
            log.error("门店更新失败: shopId={}, 影响行数={}", shopId, updateResult);
            throw new BusinessException(50000, "门店更新失败");
        }
    }

    @Override
    @Transactional
    public void updateShopStatus(Long merchantId, Long operatorId, Long shopId, Integer status) {
        log.info("更新门店状态: merchantId={}, shopId={}, status={}", merchantId, shopId, status);

        // 验证商家
        validateMerchant(merchantId);

        // 查询门店
        ShopDO shop = shopMapper.selectById(shopId);
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
        ShopDO shop = shopMapper.selectById(shopId);
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
        ShopDO shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40404, "门店不存在");
        }

        // 统计笔记数量
        LambdaQueryWrapper<NoteDO> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(NoteDO::getShopId, shopId);
        Long noteCount = noteMapper.selectCount(noteWrapper);

        // 统计评论数量
        LambdaQueryWrapper<ShopReviewDO> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(ShopReviewDO::getShopId, shopId);
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
        MerchantDO merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(40404, "商家不存在");
        }
        if (merchant.getStatus() != 1) {
            throw new BusinessException(40300, "商家账号已被禁用");
        }
    }

    /**
     * 验证类目ID是否有效
     * 检查类目是否存在且状态为启用(status=1)
     * 
     * @param categoryId 类目ID
     * @throws BusinessException 如果类目无效
     */
    private void validateCategoryId(Integer categoryId) {
        if (categoryId == null) {
            return; // 类目ID为空时不验证，使用默认值
        }

        CategoryDO category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(40400, "选择的类目不存在");
        }

        if (category.getStatus() != 1) {
            throw new BusinessException(40400, "选择的类目已被禁用");
        }
    }

    /**
     * 转换为门店列表项响应
     */
    private ShopItemVO convertToShopItemVO(ShopDO shop) {
        ShopItemVO response = new ShopItemVO();
        response.setId(shop.getId().toString());
        response.setMerchantId(shop.getMerchantId());
        response.setCategoryId(shop.getCategoryId());
        response.setName(shop.getName());
        response.setImage(shop.getHeaderImage());
        response.setHeaderImage(shop.getHeaderImage());
        response.setImages(shop.getImages());
        response.setDescription(shop.getDescription());
        response.setAddress(shop.getAddress());
        response.setLatitude(shop.getLatitude());
        response.setLongitude(shop.getLongitude());
        response.setRating(shop.getRating());
        response.setTasteScore(shop.getTasteScore());
        response.setEnvironmentScore(shop.getEnvironmentScore());
        response.setServiceScore(shop.getServiceScore());
        response.setReviewCount(shop.getReviewCount());
        response.setPopularity(shop.getPopularity());
        response.setAvgPrice(shop.getAveragePrice() != null ? shop.getAveragePrice().intValue() : null);
        response.setAveragePrice(shop.getAveragePrice());
        response.setPhone(shop.getPhone());
        response.setStatus(shop.getStatus());
        response.setBusinessHours(shop.getBusinessHours());
        response.setCreatedAt(shop.getCreatedAt());
        response.setUpdatedAt(shop.getUpdatedAt());

        // 统计笔记数量
        LambdaQueryWrapper<NoteDO> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(NoteDO::getShopId, shop.getId());
        Long noteCount = noteMapper.selectCount(noteWrapper);
        response.setNoteCount(noteCount.intValue());

        return response;
    }

    /**
     * 转换为门店详情响应
     */
    private ShopDetailVO convertToShopDetailVO(ShopDO shop) {
        ShopDetailVO response = new ShopDetailVO();
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
        response.setFavorited(false); // 商家端不需要收藏状态

        // 处理图片列表
        response.setImages(parseImages(shop.getImages()));

        return response;
    }

    /**
     * 解析图片字符串为列表
     */
    private List<String> parseImages(String images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }

        // 尝试解析JSON数组格式
        if (images.trim().startsWith("[") && images.trim().endsWith("]")) {
            try {
                // 简单的JSON数组解析
                String content = images.trim().substring(1, images.trim().length() - 1);
                if (content.isEmpty()) {
                    return new ArrayList<>();
                }
                return java.util.Arrays.stream(content.split(","))
                        .map(s -> s.trim().replaceAll("^\"|\"$", "")) // 移除引号
                        .filter(s -> !s.isEmpty())
                        .collect(java.util.stream.Collectors.toList());
            } catch (Exception e) {
                log.warn("解析图片JSON数组失败，使用逗号分割: {}", images, e);
            }
        }

        // 回退到逗号分割
        return java.util.Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }
}
