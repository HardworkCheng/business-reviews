package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.request.CreateCouponRequest;
import com.businessreviews.dto.response.CouponDetailResponse;
import com.businessreviews.dto.response.CouponItemResponse;
import com.businessreviews.entity.Coupon;
import com.businessreviews.entity.Merchant;
import com.businessreviews.entity.Shop;
import com.businessreviews.entity.UserCoupon;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.CouponMapper;
import com.businessreviews.mapper.MerchantMapper;
import com.businessreviews.mapper.ShopMapper;
import com.businessreviews.mapper.UserCouponMapper;
import com.businessreviews.service.MerchantCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商家优惠券服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantCouponServiceImpl implements MerchantCouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final MerchantMapper merchantMapper;
    private final ShopMapper shopMapper;

    @Override
    public PageResult<CouponItemResponse> getCouponList(Long merchantId, Integer pageNum, Integer pageSize, 
            Integer type, Integer status, Long shopId) {
        log.info("获取优惠券列表: merchantId={}, pageNum={}, pageSize={}, type={}, status={}", 
                merchantId, pageNum, pageSize, type, status);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 构建查询条件
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getMerchantId, merchantId);
        
        if (type != null) {
            wrapper.eq(Coupon::getType, type);
        }
        if (status != null) {
            wrapper.eq(Coupon::getStatus, status);
        }
        if (shopId != null) {
            wrapper.eq(Coupon::getShopId, shopId);
        }
        wrapper.orderByDesc(Coupon::getCreatedAt);

        
        // 分页查询
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        Page<Coupon> couponPage = couponMapper.selectPage(page, wrapper);
        
        // 转换结果
        List<CouponItemResponse> list = couponPage.getRecords().stream()
                .map(this::convertToCouponItemResponse)
                .collect(Collectors.toList());
        
        PageResult<CouponItemResponse> result = new PageResult<>();
        result.setList(list);
        result.setTotal(couponPage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public CouponDetailResponse getCouponDetail(Long merchantId, Long couponId) {
        log.info("获取优惠券详情: merchantId={}, couponId={}", merchantId, couponId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 查询优惠券
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }
        
        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权访问此优惠券");
        }
        
        return convertToCouponDetailResponse(coupon);
    }

    @Override
    @Transactional
    public Long createCoupon(Long merchantId, Long operatorId, CreateCouponRequest request) {
        log.info("创建优惠券: merchantId={}, operatorId={}", merchantId, operatorId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 创建优惠券
        Coupon coupon = new Coupon();
        coupon.setMerchantId(merchantId);
        coupon.setTitle(request.getTitle());
        coupon.setDescription(request.getDescription());
        coupon.setType(request.getType());
        
        // 根据类型设置金额或折扣
        if (request.getType() == 1 || request.getType() == 3 || request.getType() == 4) {
            // 现金券、专属券、新人券
            coupon.setAmount(request.getAmount());
            // 优先使用threshold，如果没有则使用minAmount
            coupon.setMinAmount(request.getThreshold() != null ? request.getThreshold() : request.getMinAmount());
        } else if (request.getType() == 2) {
            // 折扣券
            if (request.getDiscountRate() != null) {
                coupon.setDiscount(BigDecimal.valueOf(request.getDiscountRate() / 100.0));
            } else if (request.getDiscount() != null) {
                coupon.setDiscount(request.getDiscount());
            }
            if (request.getMaxDiscount() != null) {
                coupon.setAmount(request.getMaxDiscount());
            }
        }
        
        // 设置适用门店
        if (request.getApplicableShops() != null && !request.getApplicableShops().isEmpty()) {
            coupon.setShopId(request.getApplicableShops().get(0));
        } else if (request.getShopId() != null) {
            coupon.setShopId(request.getShopId());
        }
        
        coupon.setTotalCount(request.getTotalCount());
        coupon.setRemainCount(request.getTotalCount());
        coupon.setPerUserLimit(request.getPerUserLimit() != null ? request.getPerUserLimit() : 1);
        coupon.setStackable(request.getStackable() != null && request.getStackable() ? 1 : 0);
        
        // 设置时间
        coupon.setStartTime(request.getStartTime());
        coupon.setEndTime(request.getEndTime());
        coupon.setUseStartTime(request.getStartTime());
        coupon.setUseEndTime(request.getEndTime());
        
        coupon.setStatus(1); // 默认启用
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());
        
        couponMapper.insert(coupon);
        log.info("优惠券创建成功: couponId={}", coupon.getId());
        
        return coupon.getId();
    }

    @Override
    @Transactional
    public void updateCoupon(Long merchantId, Long operatorId, Long couponId, CreateCouponRequest request) {
        log.info("更新优惠券: merchantId={}, couponId={}", merchantId, couponId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 查询优惠券
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }
        
        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权修改此优惠券");
        }
        
        // 更新优惠券信息
        if (request.getTitle() != null) {
            coupon.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            coupon.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            coupon.setType(request.getType());
        }
        if (request.getAmount() != null) {
            coupon.setAmount(request.getAmount());
        }
        if (request.getThreshold() != null) {
            coupon.setMinAmount(request.getThreshold());
        }
        if (request.getTotalCount() != null) {
            int diff = request.getTotalCount() - coupon.getTotalCount();
            coupon.setTotalCount(request.getTotalCount());
            coupon.setRemainCount(coupon.getRemainCount() + diff);
        }
        if (request.getPerUserLimit() != null) {
            coupon.setPerUserLimit(request.getPerUserLimit());
        }
        if (request.getStartTime() != null) {
            coupon.setStartTime(request.getStartTime());
            coupon.setUseStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            coupon.setEndTime(request.getEndTime());
            coupon.setUseEndTime(request.getEndTime());
        }
        if (request.getStackable() != null) {
            coupon.setStackable(request.getStackable() ? 1 : 0);
        }
        
        coupon.setUpdatedAt(LocalDateTime.now());
        couponMapper.updateById(coupon);
        log.info("优惠券更新成功: couponId={}", couponId);
    }

    @Override
    @Transactional
    public void updateCouponStatus(Long merchantId, Long operatorId, Long couponId, Integer status) {
        log.info("更新优惠券状态: merchantId={}, couponId={}, status={}", merchantId, couponId, status);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 查询优惠券
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }
        
        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权修改此优惠券");
        }
        
        // 更新状态
        coupon.setStatus(status);
        coupon.setUpdatedAt(LocalDateTime.now());
        couponMapper.updateById(coupon);
        log.info("优惠券状态更新成功: couponId={}, status={}", couponId, status);
    }

    @Override
    public Map<String, Object> getCouponStats(Long merchantId, Long couponId) {
        log.info("获取优惠券统计: merchantId={}, couponId={}", merchantId, couponId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 查询优惠券
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }
        
        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权访问此优惠券");
        }
        
        // 统计领取数量
        LambdaQueryWrapper<UserCoupon> claimedWrapper = new LambdaQueryWrapper<>();
        claimedWrapper.eq(UserCoupon::getCouponId, couponId);
        Long totalClaimed = userCouponMapper.selectCount(claimedWrapper);
        
        // 统计已使用数量
        LambdaQueryWrapper<UserCoupon> usedWrapper = new LambdaQueryWrapper<>();
        usedWrapper.eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getStatus, 2); // 已使用
        Long totalUsed = userCouponMapper.selectCount(usedWrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIssued", coupon.getTotalCount());
        stats.put("totalClaimed", totalClaimed);
        stats.put("totalRedeemed", totalUsed);
        stats.put("remainCount", coupon.getRemainCount());
        stats.put("claimRate", coupon.getTotalCount() > 0 ? 
                (double) totalClaimed / coupon.getTotalCount() * 100 : 0.0);
        stats.put("redeemRate", totalClaimed > 0 ? 
                (double) totalUsed / totalClaimed * 100 : 0.0);
        
        return stats;
    }

    @Override
    @Transactional
    public void redeemCoupon(Long merchantId, Long operatorId, Long couponId, String code, Long shopId) {
        log.info("核销优惠券: merchantId={}, code={}, shopId={}", merchantId, code, shopId);
        
        // 验证商家
        validateMerchant(merchantId);
        
        // 根据券码查询用户优惠券
        UserCoupon userCoupon = userCouponMapper.selectByCode(code);
        if (userCoupon == null) {
            throw new BusinessException(40404, "券码无效");
        }
        
        // 验证优惠券归属
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null || !coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权核销此优惠券");
        }
        
        // 验证优惠券状态
        if (userCoupon.getStatus() == 2) {
            throw new BusinessException(40001, "优惠券已被使用");
        }
        if (userCoupon.getStatus() == 3) {
            throw new BusinessException(40002, "优惠券已过期");
        }
        
        // 验证有效期
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getUseStartTime() != null && now.isBefore(coupon.getUseStartTime())) {
            throw new BusinessException(40003, "优惠券尚未生效");
        }
        if (coupon.getUseEndTime() != null && now.isAfter(coupon.getUseEndTime())) {
            throw new BusinessException(40004, "优惠券已过期");
        }
        
        // 核销优惠券
        userCoupon.setStatus(2); // 已使用
        userCoupon.setUseTime(now);
        userCoupon.setUseShopId(shopId);
        userCoupon.setOperatorId(operatorId);
        userCouponMapper.updateById(userCoupon);
        
        log.info("优惠券核销成功: code={}", code);
    }

    @Override
    public Map<String, Object> verifyCoupon(Long merchantId, String code) {
        log.info("验证券码: merchantId={}, code={}", merchantId, code);
        
        Map<String, Object> result = new HashMap<>();
        
        // 根据券码查询用户优惠券
        UserCoupon userCoupon = userCouponMapper.selectByCode(code);
        if (userCoupon == null) {
            result.put("valid", false);
            result.put("message", "券码无效");
            return result;
        }
        
        // 查询优惠券信息
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            result.put("valid", false);
            result.put("message", "优惠券不存在");
            return result;
        }
        
        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            result.put("valid", false);
            result.put("message", "此优惠券不属于当前商家");
            return result;
        }
        
        // 验证状态
        if (userCoupon.getStatus() == 2) {
            result.put("valid", false);
            result.put("message", "优惠券已被使用");
            return result;
        }
        if (userCoupon.getStatus() == 3) {
            result.put("valid", false);
            result.put("message", "优惠券已过期");
            return result;
        }
        
        // 验证有效期
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getUseEndTime() != null && now.isAfter(coupon.getUseEndTime())) {
            result.put("valid", false);
            result.put("message", "优惠券已过期");
            return result;
        }
        
        // 返回优惠券信息
        result.put("valid", true);
        result.put("message", "券码有效");
        result.put("couponId", coupon.getId());
        result.put("title", coupon.getTitle());
        result.put("type", coupon.getType());
        result.put("amount", coupon.getAmount());
        result.put("discount", coupon.getDiscount());
        result.put("minAmount", coupon.getMinAmount());
        result.put("endTime", coupon.getUseEndTime());
        
        return result;
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
     * 转换为优惠券列表项响应
     */
    private CouponItemResponse convertToCouponItemResponse(Coupon coupon) {
        CouponItemResponse response = new CouponItemResponse();
        response.setId(coupon.getId().toString());
        response.setType(coupon.getType());
        response.setTypeName(getCouponTypeName(coupon.getType()));
        response.setTitle(coupon.getTitle());
        response.setAmount(coupon.getAmount());
        response.setDiscount(coupon.getDiscount());
        response.setMinAmount(coupon.getMinAmount());
        response.setTotalCount(coupon.getTotalCount());
        response.setRemainCount(coupon.getRemainCount());
        response.setClaimedCount(coupon.getTotalCount() - coupon.getRemainCount());
        response.setStatus(coupon.getStatus());
        response.setStatusName(getCouponStatusName(coupon.getStatus()));
        response.setStartTime(coupon.getStartTime() != null ? coupon.getStartTime().toString() : null);
        response.setEndTime(coupon.getEndTime() != null ? coupon.getEndTime().toString() : null);
        response.setCreatedAt(coupon.getCreatedAt() != null ? coupon.getCreatedAt().toString() : null);
        
        // 获取门店名称
        if (coupon.getShopId() != null) {
            Shop shop = shopMapper.selectById(coupon.getShopId());
            if (shop != null) {
                response.setShopName(shop.getName());
            }
        }
        
        return response;
    }
    
    /**
     * 转换为优惠券详情响应
     */
    private CouponDetailResponse convertToCouponDetailResponse(Coupon coupon) {
        CouponDetailResponse response = new CouponDetailResponse();
        response.setId(coupon.getId().toString());
        response.setType(coupon.getType());
        response.setTypeName(getCouponTypeName(coupon.getType()));
        response.setTitle(coupon.getTitle());
        response.setDescription(coupon.getDescription());
        response.setAmount(coupon.getAmount());
        response.setDiscount(coupon.getDiscount());
        response.setMinAmount(coupon.getMinAmount());
        response.setTotalCount(coupon.getTotalCount());
        response.setRemainCount(coupon.getRemainCount());
        response.setClaimedCount(coupon.getTotalCount() - coupon.getRemainCount());
        response.setDailyLimit(coupon.getDailyLimit());
        response.setPerUserLimit(coupon.getPerUserLimit());
        response.setShopId(coupon.getShopId() != null ? coupon.getShopId().toString() : null);
        response.setStackable(coupon.getStackable() != null && coupon.getStackable() == 1);
        response.setStatus(coupon.getStatus());
        response.setStatusName(getCouponStatusName(coupon.getStatus()));
        response.setStartTime(coupon.getStartTime() != null ? coupon.getStartTime().toString() : null);
        response.setEndTime(coupon.getEndTime() != null ? coupon.getEndTime().toString() : null);
        response.setUseStartTime(coupon.getUseStartTime() != null ? coupon.getUseStartTime().toString() : null);
        response.setUseEndTime(coupon.getUseEndTime() != null ? coupon.getUseEndTime().toString() : null);
        response.setCreatedAt(coupon.getCreatedAt() != null ? coupon.getCreatedAt().toString() : null);
        
        // 获取门店名称
        if (coupon.getShopId() != null) {
            Shop shop = shopMapper.selectById(coupon.getShopId());
            if (shop != null) {
                response.setShopName(shop.getName());
            }
        }
        
        return response;
    }
    
    /**
     * 获取优惠券类型名称
     */
    private String getCouponTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "现金券";
            case 2: return "折扣券";
            case 3: return "专属券";
            case 4: return "新人券";
            default: return "未知";
        }
    }
    
    /**
     * 获取优惠券状态名称
     */
    private String getCouponStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "启用";
            case 2: return "停用";
            case 3: return "已结束";
            default: return "未知";
        }
    }
}
