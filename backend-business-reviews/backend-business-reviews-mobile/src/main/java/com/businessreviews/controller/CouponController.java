package com.businessreviews.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.entity.Coupon;
import com.businessreviews.entity.Shop;
import com.businessreviews.entity.UserCoupon;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.CouponMapper;
import com.businessreviews.mapper.ShopMapper;
import com.businessreviews.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 优惠券控制器（移动端）
 */
@Slf4j
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final ShopMapper shopMapper;

    /**
     * 获取所有可用优惠券列表（公开接口）
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> getCouponList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        Page<Coupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getStatus, 1)
               .gt(Coupon::getEndTime, LocalDateTime.now())
               .gt(Coupon::getRemainCount, 0)
               .orderByDesc(Coupon::getCreatedAt);
        
        Page<Coupon> couponPage = couponMapper.selectPage(page, wrapper);
        
        Long userId = UserContext.getUserId();
        List<Map<String, Object>> list = couponPage.getRecords().stream()
                .map(c -> convertToCouponResponse(c, userId))
                .collect(Collectors.toList());
        
        return Result.success(PageResult.of(list, couponPage.getTotal(), pageNum, pageSize));
    }

    /**
     * 获取我的优惠券列表
     */
    @GetMapping("/my")
    public Result<PageResult<Map<String, Object>>> getMyCoupons(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        Long userId = UserContext.requireUserId();
        
        Page<UserCoupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId);
        
        // 根据状态筛选
        if ("unused".equals(status)) {
            wrapper.eq(UserCoupon::getStatus, 1);
        } else if ("used".equals(status)) {
            wrapper.eq(UserCoupon::getStatus, 2);
        } else if ("expired".equals(status)) {
            wrapper.eq(UserCoupon::getStatus, 3);
        }
        
        wrapper.orderByDesc(UserCoupon::getCreatedAt);
        
        Page<UserCoupon> userCouponPage = userCouponMapper.selectPage(page, wrapper);
        
        List<Map<String, Object>> list = userCouponPage.getRecords().stream()
                .map(this::convertToUserCouponResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        return Result.success(PageResult.of(list, userCouponPage.getTotal(), pageNum, pageSize));
    }

    /**
     * 领取优惠券
     */
    @PostMapping("/{couponId}/receive")
    public Result<?> receiveCoupon(@PathVariable Long couponId) {
        Long userId = UserContext.requireUserId();
        
        // 查询优惠券
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }
        
        // 检查优惠券状态
        if (coupon.getStatus() != 1) {
            throw new BusinessException(40001, "优惠券已停用");
        }
        
        // 检查是否过期
        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(40001, "优惠券已过期");
        }
        
        // 检查剩余数量
        if (coupon.getRemainCount() <= 0) {
            throw new BusinessException(40001, "优惠券已领完");
        }
        
        // 检查用户是否已领取
        LambdaQueryWrapper<UserCoupon> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserCoupon::getUserId, userId)
                   .eq(UserCoupon::getCouponId, couponId);
        Long count = userCouponMapper.selectCount(checkWrapper);
        
        if (coupon.getPerUserLimit() != null && count >= coupon.getPerUserLimit()) {
            throw new BusinessException(40001, "已达到领取上限");
        }
        
        // 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setCouponId(couponId);
        userCoupon.setUserId(userId);
        userCoupon.setCode(generateCouponCode());
        userCoupon.setStatus(1);
        userCoupon.setReceiveTime(LocalDateTime.now());
        userCoupon.setCreatedAt(LocalDateTime.now());
        userCouponMapper.insert(userCoupon);
        
        // 减少剩余数量
        coupon.setRemainCount(coupon.getRemainCount() - 1);
        couponMapper.updateById(coupon);
        
        log.info("用户{}领取优惠券{}成功", userId, couponId);
        return Result.success("领取成功");
    }

    /**
     * 获取优惠券详情
     */
    @GetMapping("/{couponId}")
    public Result<Map<String, Object>> getCouponDetail(@PathVariable Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }
        
        Long userId = UserContext.getUserId();
        return Result.success(convertToCouponResponse(coupon, userId));
    }

    /**
     * 转换优惠券响应
     */
    private Map<String, Object> convertToCouponResponse(Coupon coupon, Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", coupon.getId());
        map.put("type", coupon.getType());
        map.put("title", coupon.getTitle());
        map.put("description", coupon.getDescription());
        map.put("amount", coupon.getAmount());
        map.put("discount", coupon.getDiscount());
        map.put("minAmount", coupon.getMinAmount());
        map.put("remainCount", coupon.getRemainCount());
        map.put("startTime", coupon.getStartTime());
        map.put("endTime", coupon.getEndTime());
        map.put("shopId", coupon.getShopId());
        
        // 获取商家名称
        if (coupon.getShopId() != null) {
            Shop shop = shopMapper.selectById(coupon.getShopId());
            if (shop != null) {
                map.put("shopName", shop.getName());
            }
        }
        
        // 检查用户是否已领取
        if (userId != null) {
            LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserCoupon::getUserId, userId)
                   .eq(UserCoupon::getCouponId, coupon.getId());
            Long count = userCouponMapper.selectCount(wrapper);
            map.put("isReceived", count > 0);
        } else {
            map.put("isReceived", false);
        }
        
        return map;
    }

    /**
     * 转换用户优惠券响应
     */
    private Map<String, Object> convertToUserCouponResponse(UserCoupon userCoupon) {
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", coupon.getId());
        map.put("userCouponId", userCoupon.getId());
        map.put("type", coupon.getType());
        map.put("title", coupon.getTitle());
        map.put("description", coupon.getDescription());
        map.put("amount", coupon.getAmount());
        map.put("discount", coupon.getDiscount());
        map.put("minAmount", coupon.getMinAmount());
        map.put("startTime", coupon.getStartTime());
        map.put("endTime", coupon.getEndTime());
        map.put("shopId", coupon.getShopId());
        map.put("code", userCoupon.getCode());
        map.put("receiveTime", userCoupon.getReceiveTime());
        
        // 状态转换
        String status;
        if (userCoupon.getStatus() == 2) {
            status = "used";
        } else if (userCoupon.getStatus() == 3 || coupon.getEndTime().isBefore(LocalDateTime.now())) {
            status = "expired";
        } else {
            status = "received";
        }
        map.put("userStatus", status);
        
        // 获取商家名称
        if (coupon.getShopId() != null) {
            Shop shop = shopMapper.selectById(coupon.getShopId());
            if (shop != null) {
                map.put("shopName", shop.getName());
            }
        }
        
        return map;
    }

    /**
     * 生成优惠券码
     */
    private String generateCouponCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
