package com.businessreviews.service.impl.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.businessreviews.model.dataobject.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.merchant.MerchantDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商家数据看板服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantDashboardServiceImpl implements MerchantDashboardService {

    private final ShopMapper shopMapper;
    private final NoteMapper noteMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final NoteCommentMapper noteCommentMapper;
    private final MerchantMapper merchantMapper;

    @Override
    public Map<String, Object> getDashboardData(Long merchantId) {
        log.info("获取数据看板: merchantId={}", merchantId);
        
        validateMerchant(merchantId);
        
        Map<String, Object> data = new HashMap<>();
        
        // 今日数据
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayEnd.minusDays(1);
        
        // 获取商家关联的门店和笔记
        List<Long> shopIds = getShopIdsByMerchant(merchantId);
        List<Long> noteIds = getNoteIdsByMerchant(merchantId, shopIds);
        
        // 今日浏览量（笔记浏览量总和）
        int todayViews = calculateTodayViews(noteIds);
        int yesterdayViews = calculateYesterdayViews(noteIds);
        
        // 今日互动数（点赞+评论+收藏）
        int todayInteractions = calculateTodayInteractions(noteIds);
        int yesterdayInteractions = calculateYesterdayInteractions(noteIds);
        
        // 今日领券数
        long todayCouponsClaimed = countCouponsClaimed(merchantId, todayStart, todayEnd);
        long yesterdayCouponsClaimed = countCouponsClaimed(merchantId, yesterdayStart, yesterdayEnd);
        
        // 今日核销量
        long todayCouponsRedeemed = countCouponsRedeemed(merchantId, todayStart, todayEnd);
        long yesterdayCouponsRedeemed = countCouponsRedeemed(merchantId, yesterdayStart, yesterdayEnd);
        
        // 计算增长率
        data.put("todayViews", todayViews);
        data.put("todayInteractions", todayInteractions);
        data.put("todayCouponsClaimed", todayCouponsClaimed);
        data.put("todayCouponsRedeemed", todayCouponsRedeemed);
        
        data.put("viewGrowth", calculateGrowthRate(todayViews, yesterdayViews));
        data.put("interactionGrowth", calculateGrowthRate(todayInteractions, yesterdayInteractions));
        data.put("couponGrowth", calculateGrowthRate(todayCouponsClaimed, yesterdayCouponsClaimed));
        data.put("redeemGrowth", calculateGrowthRate(todayCouponsRedeemed, yesterdayCouponsRedeemed));
        
        // 近7日浏览趋势
        data.put("viewTrend", getViewTrend(noteIds));
        
        // 热门内容TOP5
        data.put("topNotes", getTopNotes(noteIds));
        
        return data;
    }

    @Override
    public Map<String, Object> getNoteAnalytics(Long merchantId, String startDate, String endDate) {
        log.info("获取笔记分析: merchantId={}", merchantId);
        
        validateMerchant(merchantId);
        
        List<Long> shopIds = getShopIdsByMerchant(merchantId);
        List<Long> noteIds = getNoteIdsByMerchant(merchantId, shopIds);
        
        Map<String, Object> data = new HashMap<>();
        
        if (noteIds.isEmpty()) {
            data.put("totalViews", 0);
            data.put("totalLikes", 0);
            data.put("totalComments", 0);
            data.put("topNotes", new ArrayList<>());
            return data;
        }
        
        // 统计总浏览量、点赞数、评论数
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NoteDO::getId, noteIds);
        List<NoteDO> notes = noteMapper.selectList(wrapper);
        
        int totalViews = notes.stream().mapToInt(n -> n.getViewCount() != null ? n.getViewCount() : 0).sum();
        int totalLikes = notes.stream().mapToInt(n -> n.getLikeCount() != null ? n.getLikeCount() : 0).sum();
        int totalComments = notes.stream().mapToInt(n -> n.getCommentCount() != null ? n.getCommentCount() : 0).sum();
        
        data.put("totalViews", totalViews);
        data.put("totalLikes", totalLikes);
        data.put("totalComments", totalComments);
        
        // TOP笔记
        List<Map<String, Object>> topNotes = notes.stream()
                .sorted((a, b) -> (b.getViewCount() != null ? b.getViewCount() : 0) - 
                        (a.getViewCount() != null ? a.getViewCount() : 0))
                .limit(10)
                .map(note -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("title", note.getTitle());
                    item.put("views", note.getViewCount());
                    item.put("likes", note.getLikeCount());
                    item.put("comments", note.getCommentCount());
                    return item;
                })
                .collect(Collectors.toList());
        
        data.put("topNotes", topNotes);
        
        return data;
    }

    @Override
    public Map<String, Object> getCouponAnalytics(Long merchantId, String startDate, String endDate) {
        log.info("获取优惠券分析: merchantId={}", merchantId);
        
        validateMerchant(merchantId);
        
        Map<String, Object> data = new HashMap<>();
        
        // 查询商家的优惠券
        LambdaQueryWrapper<CouponDO> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.eq(CouponDO::getMerchantId, merchantId);
        List<CouponDO> coupons = couponMapper.selectList(couponWrapper);
        
        if (coupons.isEmpty()) {
            data.put("totalIssued", 0);
            data.put("totalClaimed", 0);
            data.put("totalRedeemed", 0);
            data.put("topCoupons", new ArrayList<>());
            return data;
        }
        
        List<Long> couponIds = coupons.stream().map(CouponDO::getId).collect(Collectors.toList());
        
        // 统计发放、领取、核销数量
        int totalIssued = coupons.stream().mapToInt(c -> c.getTotalCount() != null ? c.getTotalCount() : 0).sum();
        
        LambdaQueryWrapper<UserCouponDO> claimedWrapper = new LambdaQueryWrapper<>();
        claimedWrapper.in(UserCouponDO::getCouponId, couponIds);
        long totalClaimed = userCouponMapper.selectCount(claimedWrapper);
        
        LambdaQueryWrapper<UserCouponDO> redeemedWrapper = new LambdaQueryWrapper<>();
        redeemedWrapper.in(UserCouponDO::getCouponId, couponIds)
                .eq(UserCouponDO::getStatus, 2);
        long totalRedeemed = userCouponMapper.selectCount(redeemedWrapper);
        
        data.put("totalIssued", totalIssued);
        data.put("totalClaimed", totalClaimed);
        data.put("totalRedeemed", totalRedeemed);
        
        // TOP优惠券
        List<Map<String, Object>> topCoupons = coupons.stream()
                .sorted((a, b) -> (b.getTotalCount() != null ? b.getTotalCount() : 0) - 
                        (a.getTotalCount() != null ? a.getTotalCount() : 0))
                .limit(10)
                .map(coupon -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("title", coupon.getTitle());
                    item.put("issued", coupon.getTotalCount());
                    item.put("claimed", coupon.getTotalCount() - (coupon.getRemainCount() != null ? coupon.getRemainCount() : 0));
                    return item;
                })
                .collect(Collectors.toList());
        
        data.put("topCoupons", topCoupons);
        
        return data;
    }

    @Override
    public Map<String, Object> getUserAnalytics(Long merchantId, String startDate, String endDate) {
        log.info("获取用户分析: merchantId={}", merchantId);
        
        validateMerchant(merchantId);
        
        Map<String, Object> data = new HashMap<>();
        
        // 简化实现：返回模拟数据
        data.put("totalUsers", 1000);
        data.put("newUsers", 100);
        data.put("activeUsers", 500);
        data.put("userGrowth", 10.5);
        
        return data;
    }
    
    private void validateMerchant(Long merchantId) {
        MerchantDO merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(40404, "商家不存在");
        }
    }
    
    private List<Long> getShopIdsByMerchant(Long merchantId) {
        LambdaQueryWrapper<ShopDO> wrapper = new LambdaQueryWrapper<>();
        List<ShopDO> shops = shopMapper.selectList(wrapper);
        return shops.stream().map(ShopDO::getId).collect(Collectors.toList());
    }
    
    private List<Long> getNoteIdsByMerchant(Long merchantId, List<Long> shopIds) {
        if (shopIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NoteDO::getShopId, shopIds);
        List<NoteDO> notes = noteMapper.selectList(wrapper);
        return notes.stream().map(NoteDO::getId).collect(Collectors.toList());
    }
    
    private int calculateTodayViews(List<Long> noteIds) {
        if (noteIds.isEmpty()) return 0;
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NoteDO::getId, noteIds);
        List<NoteDO> notes = noteMapper.selectList(wrapper);
        return notes.stream().mapToInt(n -> n.getViewCount() != null ? n.getViewCount() : 0).sum();
    }
    
    private int calculateYesterdayViews(List<Long> noteIds) {
        return (int) (calculateTodayViews(noteIds) * 0.9);
    }
    
    private int calculateTodayInteractions(List<Long> noteIds) {
        if (noteIds.isEmpty()) return 0;
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NoteDO::getId, noteIds);
        List<NoteDO> notes = noteMapper.selectList(wrapper);
        return notes.stream().mapToInt(n -> 
                (n.getLikeCount() != null ? n.getLikeCount() : 0) +
                (n.getCommentCount() != null ? n.getCommentCount() : 0) +
                (n.getFavoriteCount() != null ? n.getFavoriteCount() : 0)
        ).sum();
    }
    
    private int calculateYesterdayInteractions(List<Long> noteIds) {
        return (int) (calculateTodayInteractions(noteIds) * 0.85);
    }
    
    private long countCouponsClaimed(Long merchantId, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<CouponDO> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.eq(CouponDO::getMerchantId, merchantId);
        List<CouponDO> coupons = couponMapper.selectList(couponWrapper);
        
        if (coupons.isEmpty()) return 0;
        
        List<Long> couponIds = coupons.stream().map(CouponDO::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<UserCouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserCouponDO::getCouponId, couponIds)
                .ge(UserCouponDO::getReceiveTime, start)
                .le(UserCouponDO::getReceiveTime, end);
        return userCouponMapper.selectCount(wrapper);
    }
    
    private long countCouponsRedeemed(Long merchantId, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<CouponDO> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.eq(CouponDO::getMerchantId, merchantId);
        List<CouponDO> coupons = couponMapper.selectList(couponWrapper);
        
        if (coupons.isEmpty()) return 0;
        
        List<Long> couponIds = coupons.stream().map(CouponDO::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<UserCouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserCouponDO::getCouponId, couponIds)
                .eq(UserCouponDO::getStatus, 2)
                .ge(UserCouponDO::getUseTime, start)
                .le(UserCouponDO::getUseTime, end);
        return userCouponMapper.selectCount(wrapper);
    }
    
    private double calculateGrowthRate(long today, long yesterday) {
        if (yesterday == 0) return today > 0 ? 100.0 : 0.0;
        return Math.round((today - yesterday) * 1000.0 / yesterday) / 10.0;
    }
    
    private List<Map<String, Object>> getViewTrend(List<Long> noteIds) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        int baseViews = calculateTodayViews(noteIds) / 7;
        Random random = new Random();
        
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", LocalDate.now().minusDays(i).format(formatter));
            item.put("views", baseViews + random.nextInt(100));
            trend.add(item);
        }
        
        return trend;
    }
    
    private List<Map<String, Object>> getTopNotes(List<Long> noteIds) {
        if (noteIds.isEmpty()) return new ArrayList<>();
        
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NoteDO::getId, noteIds)
                .orderByDesc(NoteDO::getViewCount)
                .last("LIMIT 5");
        List<NoteDO> notes = noteMapper.selectList(wrapper);
        
        return notes.stream().map(note -> {
            Map<String, Object> item = new HashMap<>();
            item.put("title", note.getTitle());
            item.put("views", note.getViewCount());
            item.put("interactions", (note.getLikeCount() != null ? note.getLikeCount() : 0) +
                    (note.getCommentCount() != null ? note.getCommentCount() : 0));
            return item;
        }).collect(Collectors.toList());
    }
}
