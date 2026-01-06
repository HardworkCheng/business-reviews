package com.businessreviews.web.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.model.dataobject.CouponDO;
import com.businessreviews.model.dataobject.ShopDO;
import com.businessreviews.model.dataobject.UserCouponDO;
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
 * <p>
 * 处理移动端优惠券的领取、查看、列表查询等操作。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@RestController
@RequestMapping("/app/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final ShopMapper shopMapper;

    /**
     * 获取所有可用优惠券列表（公开接口）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 优惠券列表
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> getCouponList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Page<CouponDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponDO::getStatus, 1)
                .gt(CouponDO::getEndTime, LocalDateTime.now())
                .gt(CouponDO::getRemainCount, 0)
                .orderByDesc(CouponDO::getCreatedAt);

        Page<CouponDO> couponPage = couponMapper.selectPage(page, wrapper);

        Long userId = UserContext.getUserId();
        List<Map<String, Object>> list = couponPage.getRecords().stream()
                .map(c -> convertToCouponResponse(c, userId))
                .collect(Collectors.toList());

        return Result.success(PageResult.of(list, couponPage.getTotal(), pageNum, pageSize));
    }

    /**
     * 获取可领取的优惠券列表（领券中心）
     * <p>
     * 显示所有状态为启用、未过期、有剩余数量的优惠券。
     * </p>
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param keyword  关键词
     * @param type     类型
     * @return 优惠券列表
     */
    @GetMapping("/available")
    public Result<PageResult<Map<String, Object>>> getAvailableCoupons(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type) {

        Page<CouponDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponDO::getStatus, 1)
                .gt(CouponDO::getEndTime, LocalDateTime.now())
                .gt(CouponDO::getRemainCount, 0);

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(CouponDO::getTitle, keyword.trim());
        }

        // 类型筛选
        if (type != null) {
            wrapper.eq(CouponDO::getType, type);
        }

        wrapper.orderByDesc(CouponDO::getCreatedAt);

        Page<CouponDO> couponPage = couponMapper.selectPage(page, wrapper);

        Long userId = UserContext.getUserId();
        List<Map<String, Object>> list = couponPage.getRecords().stream()
                .map(c -> convertToCouponResponse(c, userId))
                .collect(Collectors.toList());

        return Result.success(PageResult.of(list, couponPage.getTotal(), pageNum, pageSize));
    }

    /**
     * 获取我的优惠券列表
     *
     * @param status   状态 (all/unused/used/expired)
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 我的优惠券列表
     */
    @GetMapping("/my")
    public Result<PageResult<Map<String, Object>>> getMyCoupons(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long userId = UserContext.requireUserId();

        Page<UserCouponDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserCouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCouponDO::getUserId, userId);

        // 根据状态筛选
        if ("unused".equals(status)) {
            wrapper.eq(UserCouponDO::getStatus, 1);
        } else if ("used".equals(status)) {
            wrapper.eq(UserCouponDO::getStatus, 2);
        } else if ("expired".equals(status)) {
            wrapper.eq(UserCouponDO::getStatus, 3);
        }

        wrapper.orderByDesc(UserCouponDO::getReceiveTime);

        Page<UserCouponDO> userCouponPage = userCouponMapper.selectPage(page, wrapper);

        List<Map<String, Object>> list = userCouponPage.getRecords().stream()
                .map(this::convertToUserCouponResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(list, userCouponPage.getTotal(), pageNum, pageSize));
    }

    /**
     * 领取优惠券
     *
     * @param couponId 优惠券ID
     * @return 成功结果
     */
    @PostMapping("/{couponId}/claim")
    public Result<?> claimCoupon(@PathVariable Long couponId) {
        Long userId = UserContext.requireUserId();

        // 查询优惠券
        CouponDO coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }

        // 检查优惠券状态
        if (coupon.getStatus() != 1) {
            throw new BusinessException(40001, "优惠券已停用");
        }

        // 检查是否已开始
        if (coupon.getStartTime() != null && coupon.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(40001, "优惠券活动尚未开始");
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
        LambdaQueryWrapper<UserCouponDO> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserCouponDO::getUserId, userId)
                .eq(UserCouponDO::getCouponId, couponId);
        Long count = userCouponMapper.selectCount(checkWrapper);

        if (coupon.getPerUserLimit() != null && count >= coupon.getPerUserLimit()) {
            throw new BusinessException(40001, "已达到领取上限");
        }

        // 创建用户优惠券记录
        UserCouponDO userCoupon = new UserCouponDO();
        userCoupon.setCouponId(couponId);
        userCoupon.setUserId(userId);
        userCoupon.setCode(generateCouponCode());
        userCoupon.setStatus(1);
        userCoupon.setReceiveTime(LocalDateTime.now());
        userCouponMapper.insert(userCoupon);

        // 减少剩余数量
        coupon.setRemainCount(coupon.getRemainCount() - 1);
        couponMapper.updateById(coupon);

        log.info("用户{}领取优惠券{}成功", userId, couponId);
        return Result.success("领取成功");
    }

    /**
     * 领取优惠券（兼容旧接口）
     *
     * @param couponId 优惠券ID
     * @return 成功结果
     */
    @PostMapping("/{couponId}/receive")
    public Result<?> receiveCoupon(@PathVariable Long couponId) {
        return claimCoupon(couponId);
    }

    /**
     * 获取优惠券详情
     *
     * @param couponId 优惠券ID
     * @return 优惠券详情
     */
    @GetMapping("/{couponId}")
    public Result<Map<String, Object>> getCouponDetail(@PathVariable Long couponId) {
        CouponDO coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }

        Long userId = UserContext.getUserId();
        return Result.success(convertToCouponResponse(coupon, userId));
    }

    /**
     * 转换优惠券响应
     */
    private Map<String, Object> convertToCouponResponse(CouponDO coupon, Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", coupon.getId());
        map.put("type", coupon.getType());
        map.put("title", coupon.getTitle());
        map.put("description", coupon.getDescription());
        map.put("amount", coupon.getAmount());
        map.put("discount", coupon.getDiscount());
        map.put("minAmount", coupon.getMinAmount());
        map.put("totalCount", coupon.getTotalCount());
        map.put("remainCount", coupon.getRemainCount());
        map.put("perUserLimit", coupon.getPerUserLimit());
        map.put("startTime", coupon.getStartTime());
        map.put("endTime", coupon.getEndTime());
        map.put("stackable", coupon.getStackable());
        map.put("shopId", coupon.getShopId());

        // 根据类型格式化使用条件
        String condition = formatCouponCondition(coupon);
        map.put("condition", condition);

        // 计算优惠券状态
        String status = "available";
        LocalDateTime now = LocalDateTime.now();

        if (coupon.getRemainCount() <= 0) {
            status = "sold_out";
        } else if (coupon.getStartTime() != null && coupon.getStartTime().isAfter(now)) {
            status = "not_started";
        }

        // 获取商家名称和图片
        if (coupon.getShopId() != null) {
            ShopDO shop = shopMapper.selectById(coupon.getShopId());
            if (shop != null) {
                map.put("shopName", shop.getName());
                map.put("shopLogo", shop.getHeaderImage());
            }
        }

        // 检查用户是否已领取
        if (userId != null) {
            LambdaQueryWrapper<UserCouponDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserCouponDO::getUserId, userId)
                    .eq(UserCouponDO::getCouponId, coupon.getId());
            Long count = userCouponMapper.selectCount(wrapper);
            map.put("isReceived", count > 0);
            if (count > 0 && !"sold_out".equals(status)) {
                status = "claimed";
            }
        } else {
            map.put("isReceived", false);
        }

        map.put("status", status);

        return map;
    }

    /**
     * 格式化优惠券使用条件
     */
    private String formatCouponCondition(CouponDO coupon) {
        Integer type = coupon.getType();
        java.math.BigDecimal minAmount = coupon.getMinAmount();

        if (type == 3) {
            return "无门槛";
        } else if (minAmount != null && minAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            return "满" + minAmount.intValue() + "可用";
        } else {
            return "无门槛";
        }
    }

    /**
     * 转换用户优惠券响应
     */
    private Map<String, Object> convertToUserCouponResponse(UserCouponDO userCoupon) {
        CouponDO coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null)
            return null;

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
        map.put("expireTime", coupon.getEndTime());
        map.put("shopId", coupon.getShopId());
        map.put("code", userCoupon.getCode());
        map.put("receiveTime", userCoupon.getReceiveTime());

        // 格式化使用条件
        map.put("condition", formatCouponCondition(coupon));

        // 状态转换: unused/used/expired
        String status;
        if (userCoupon.getStatus() == 2) {
            status = "used";
        } else if (userCoupon.getStatus() == 3 || coupon.getEndTime().isBefore(LocalDateTime.now())) {
            status = "expired";
        } else {
            status = "unused";
        }
        map.put("status", status);
        map.put("userStatus", status);

        // 获取商家名称和图片
        if (coupon.getShopId() != null) {
            ShopDO shop = shopMapper.selectById(coupon.getShopId());
            if (shop != null) {
                map.put("shopName", shop.getName());
                map.put("shopLogo", shop.getHeaderImage());
            }
        } else {
            map.put("shopName", "全部商家");
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
