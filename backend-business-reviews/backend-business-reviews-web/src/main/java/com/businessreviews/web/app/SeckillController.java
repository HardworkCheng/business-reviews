package com.businessreviews.web.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.model.dataobject.CouponDO;
import com.businessreviews.model.dataobject.UserCouponDO;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.CouponMapper;
import com.businessreviews.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 秒杀活动控制器（移动端）
 */
@Slf4j
@RestController
@RequestMapping("/app/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    /**
     * 获取秒杀活动列表
     * 注：当前简化实现，直接返回标记为秒杀的优惠券
     * 后续可扩展为独立的秒杀活动表
     */
    @GetMapping("/activities")
    public Result<Map<String, Object>> getSeckillActivities(
            @RequestParam(defaultValue = "2") Integer status) {
        
        // 模拟秒杀活动数据
        // 实际应从 seckill_activities 表查询
        Map<String, Object> activity = new HashMap<>();
        activity.put("id", 1L);
        activity.put("title", "限时秒杀");
        
        // 设置活动结束时间为今天23:59:59
        LocalDateTime endTime = LocalDateTime.now()
                .withHour(23).withMinute(59).withSecond(59);
        activity.put("startTime", LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        activity.put("endTime", endTime);
        activity.put("status", 2); // 进行中
        
        // 获取秒杀优惠券列表
        // 实际应从 seckill_coupons 关联表查询
        // 这里简化为查询所有可用优惠券的前几个作为秒杀券
        LambdaQueryWrapper<CouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponDO::getStatus, 1)
               .gt(CouponDO::getEndTime, LocalDateTime.now())
               .gt(CouponDO::getRemainCount, 0)
               .orderByDesc(CouponDO::getCreatedAt)
               .last("LIMIT 5");
        
        List<CouponDO> coupons = couponMapper.selectList(wrapper);
        
        List<Map<String, Object>> seckillCoupons = new ArrayList<>();
        Random random = new Random();
        
        for (CouponDO coupon : coupons) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", coupon.getId());
            item.put("title", coupon.getTitle());
            
            // 模拟秒杀价格（原价的30%-50%）
            double originalPrice = coupon.getAmount() != null ? 
                    coupon.getAmount().doubleValue() * 2 : 20.0;
            double seckillPrice = originalPrice * (0.3 + random.nextDouble() * 0.2);
            
            item.put("seckillPrice", Math.round(seckillPrice * 10) / 10.0);
            item.put("originalPrice", originalPrice);
            item.put("seckillStock", coupon.getTotalCount());
            item.put("remainStock", coupon.getRemainCount());
            
            seckillCoupons.add(item);
        }
        
        activity.put("coupons", seckillCoupons);
        
        return Result.success(activity);
    }

    /**
     * 领取秒杀券
     */
    @PostMapping("/{seckillId}/coupons/{couponId}/claim")
    public Result<Map<String, Object>> claimSeckillCoupon(
            @PathVariable Long seckillId,
            @PathVariable Long couponId) {
        
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
        
        // 检查是否过期
        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(40001, "优惠券已过期");
        }
        
        // 检查剩余数量（秒杀券需要更严格的库存检查）
        synchronized (this) {
            // 重新查询库存
            coupon = couponMapper.selectById(couponId);
            if (coupon.getRemainCount() <= 0) {
                throw new BusinessException(40001, "优惠券已抢光");
            }
            
            // 检查用户是否已领取该秒杀券
            LambdaQueryWrapper<UserCouponDO> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(UserCouponDO::getUserId, userId)
                       .eq(UserCouponDO::getCouponId, couponId);
            Long count = userCouponMapper.selectCount(checkWrapper);
            
            // 秒杀券每人限领1张
            if (count >= 1) {
                throw new BusinessException(40001, "您已领取过该秒杀券");
            }
            
            // 创建用户优惠券记录
            UserCouponDO userCoupon = new UserCouponDO();
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
            
            log.info("用户{}抢购秒杀券{}成功", userId, couponId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userCouponId", userCoupon.getId());
            result.put("code", userCoupon.getCode());
            
            return Result.success(result);
        }
    }

    /**
     * 生成优惠券码
     */
    private String generateCouponCode() {
        return "SK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
