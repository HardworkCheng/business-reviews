package com.businessreviews.service.impl.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.DefaultAvatar;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.CommentVO;
import com.businessreviews.model.dataobject.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.merchant.MerchantCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商家评论服务实现类
 * 管理商家评价（shop_reviews表）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantCommentServiceImpl implements MerchantCommentService {

    private final ShopReviewMapper shopReviewMapper;
    private final ShopMapper shopMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<CommentVO> getCommentList(Long merchantId, Long shopId, Integer pageNum, Integer pageSize,
            Integer status, String keyword, Boolean isNegative) {
        log.info("获取商家评论列表: merchantId={}, shopId={}, pageNum={}, pageSize={}, isNegative={}",
                merchantId, shopId, pageNum, pageSize, isNegative);

        validateMerchant(merchantId);

        // 获取门店ID列表
        List<Long> shopIds;
        if (shopId != null) {
            // 如果指定了门店ID，只获取该门店的评论
            validateShopBelongsToMerchant(merchantId, shopId);
            shopIds = Collections.singletonList(shopId);
        } else {
            // 如果没有指定门店ID，获取商家所有门店
            shopIds = getShopIdsByMerchant(merchantId);
        }

        if (shopIds.isEmpty()) {
            return emptyPageResult(pageNum, pageSize);
        }

        LambdaQueryWrapper<ShopReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ShopReviewDO::getShopId, shopIds);

        if (status != null) {
            wrapper.eq(ShopReviewDO::getStatus, status);
        }

        // 如果是查询差评，添加评分筛选条件
        if (isNegative != null && isNegative) {
            wrapper.lt(ShopReviewDO::getRating, 3);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(ShopReviewDO::getContent, keyword);
        }
        wrapper.orderByDesc(ShopReviewDO::getCreatedAt);

        Page<ShopReviewDO> page = new Page<>(pageNum, pageSize);
        Page<ShopReviewDO> reviewPage = shopReviewMapper.selectPage(page, wrapper);

        List<CommentVO> list = reviewPage.getRecords().stream()
                .map(this::convertToCommentVO)
                .collect(Collectors.toList());

        // 计算Tab计数
        Map<String, Long> tabCounts = calculateTabCounts(shopIds);

        PageResult<CommentVO> result = new PageResult<>();
        result.setList(list);
        result.setTotal(reviewPage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTabCounts(tabCounts);
        return result;
    }

    private Map<String, Long> calculateTabCounts(List<Long> shopIds) {
        Map<String, Long> counts = new HashMap<>();

        // 全部评论
        LambdaQueryWrapper<ShopReviewDO> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.in(ShopReviewDO::getShopId, shopIds);
        long all = shopReviewMapper.selectCount(allWrapper);

        // 正常显示
        LambdaQueryWrapper<ShopReviewDO> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.in(ShopReviewDO::getShopId, shopIds)
                .eq(ShopReviewDO::getStatus, 1);
        long published = shopReviewMapper.selectCount(publishedWrapper);

        // 已删除
        LambdaQueryWrapper<ShopReviewDO> deletedWrapper = new LambdaQueryWrapper<>();
        deletedWrapper.in(ShopReviewDO::getShopId, shopIds)
                .eq(ShopReviewDO::getStatus, 2);
        long deleted = shopReviewMapper.selectCount(deletedWrapper);

        // 差评/投诉（评分低于3分的）
        LambdaQueryWrapper<ShopReviewDO> negativeWrapper = new LambdaQueryWrapper<>();
        negativeWrapper.in(ShopReviewDO::getShopId, shopIds)
                .lt(ShopReviewDO::getRating, 3)
                .eq(ShopReviewDO::getStatus, 1);
        long negative = shopReviewMapper.selectCount(negativeWrapper);

        counts.put("all", all);
        counts.put("published", published);
        counts.put("deleted", deleted);
        counts.put("negative", negative);

        return counts;
    }

    @Override
    @Transactional
    public void replyComment(Long merchantId, Long operatorId, Long commentId, String content) {
        log.info("回复商家评论: merchantId={}, commentId={}", merchantId, commentId);

        validateMerchant(merchantId);

        ShopReviewDO review = shopReviewMapper.selectById(commentId);
        if (review == null) {
            throw new BusinessException(40404, "评论不存在");
        }

        // 验证评论属于该商家的门店
        ShopDO shop = shopMapper.selectById(review.getShopId());
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException(40300, "无权回复该评论");
        }

        // 更新回复内容
        review.setReply(content);
        review.setReplyTime(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        shopReviewMapper.updateById(review);

        log.info("商家评论回复成功: reviewId={}", review.getId());
    }

    @Override
    @Transactional
    public void deleteComment(Long merchantId, Long operatorId, Long commentId) {
        log.info("删除商家评论: merchantId={}, commentId={}", merchantId, commentId);

        validateMerchant(merchantId);

        ShopReviewDO review = shopReviewMapper.selectById(commentId);
        if (review == null) {
            throw new BusinessException(40404, "评论不存在");
        }

        // 检查每日删除限额 (每天最多2条)
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Long> shopIds = getShopIdsByMerchant(merchantId);
        if (!shopIds.isEmpty()) {
            LambdaQueryWrapper<ShopReviewDO> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.in(ShopReviewDO::getShopId, shopIds)
                    .eq(ShopReviewDO::getStatus, 2) // 已删除状态
                    .ge(ShopReviewDO::getUpdatedAt, todayStart);
            Long todayDeletedCount = shopReviewMapper.selectCount(countWrapper);
            if (todayDeletedCount >= 2) {
                throw new BusinessException(40300, "每天最多只能删除2条评论");
            }
        }

        // 验证评论属于该商家的门店
        ShopDO shop = shopMapper.selectById(review.getShopId());
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException(40300, "无权删除该评论");
        }

        // 软删除：更新状态为隐藏
        review.setStatus(2);
        review.setUpdatedAt(LocalDateTime.now());
        shopReviewMapper.updateById(review);

        log.info("商家评论删除成功: reviewId={}", commentId);
    }

    @Override
    public Map<String, Object> getCommentStats(Long merchantId) {
        log.info("获取商家评论统计: merchantId={}", merchantId);

        validateMerchant(merchantId);

        List<Long> shopIds = getShopIdsByMerchant(merchantId);

        long totalComments = 0;
        long pendingComments = 0;

        if (!shopIds.isEmpty()) {
            LambdaQueryWrapper<ShopReviewDO> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.in(ShopReviewDO::getShopId, shopIds);
            totalComments = shopReviewMapper.selectCount(totalWrapper);

            LambdaQueryWrapper<ShopReviewDO> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.in(ShopReviewDO::getShopId, shopIds)
                    .eq(ShopReviewDO::getStatus, 1)
                    .isNull(ShopReviewDO::getReply);
            pendingComments = shopReviewMapper.selectCount(pendingWrapper);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalComments", totalComments);
        stats.put("pendingComments", pendingComments);
        stats.put("repliedComments", totalComments - pendingComments);

        return stats;
    }

    @Override
    public Map<String, Object> getDashboard(Long merchantId, Long shopId) {
        log.info("获取数据概览: merchantId={}, shopId={}", merchantId, shopId);

        validateMerchant(merchantId);

        // 获取门店ID列表
        List<Long> shopIds;
        if (shopId != null) {
            // 如果指定了门店ID，只获取该门店的评论
            validateShopBelongsToMerchant(merchantId, shopId);
            shopIds = Collections.singletonList(shopId);
        } else {
            // 如果没有指定门店ID，获取商家所有门店
            shopIds = getShopIdsByMerchant(merchantId);
        }

        Map<String, Object> dashboard = new HashMap<>();

        if (shopIds.isEmpty()) {
            dashboard.put("todayNewComments", 0);
            dashboard.put("todayTrend", 0);
            dashboard.put("averageRating", 0.0);
            dashboard.put("ratingTrend", 0);
            dashboard.put("pendingReply", 0);
            dashboard.put("replyTrend", 0);
            dashboard.put("negativeComments", 0);
            return dashboard;
        }

        // 今日新增评论
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<ShopReviewDO> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.in(ShopReviewDO::getShopId, shopIds)
                .ge(ShopReviewDO::getCreatedAt, todayStart);
        long todayNewComments = shopReviewMapper.selectCount(todayWrapper);

        // 昨日新增评论（用于计算趋势）
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LambdaQueryWrapper<ShopReviewDO> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.in(ShopReviewDO::getShopId, shopIds)
                .ge(ShopReviewDO::getCreatedAt, yesterdayStart)
                .lt(ShopReviewDO::getCreatedAt, todayStart);
        long yesterdayComments = shopReviewMapper.selectCount(yesterdayWrapper);

        // 计算趋势
        int todayTrend = 0;
        if (yesterdayComments > 0) {
            todayTrend = (int) (((todayNewComments - yesterdayComments) * 100.0) / yesterdayComments);
        } else if (todayNewComments > 0) {
            todayTrend = 100;
        }

        // 计算平均评分
        LambdaQueryWrapper<ShopReviewDO> ratingWrapper = new LambdaQueryWrapper<>();
        ratingWrapper.in(ShopReviewDO::getShopId, shopIds)
                .eq(ShopReviewDO::getStatus, 1);
        List<ShopReviewDO> reviews = shopReviewMapper.selectList(ratingWrapper);
        double averageRating = reviews.stream()
                .mapToDouble(r -> r.getRating().doubleValue())
                .average()
                .orElse(0.0);

        // 待回复内容（没有回复的评论）
        LambdaQueryWrapper<ShopReviewDO> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.in(ShopReviewDO::getShopId, shopIds)
                .eq(ShopReviewDO::getStatus, 1)
                .isNull(ShopReviewDO::getReply);
        long pendingReply = shopReviewMapper.selectCount(pendingWrapper);

        // 差评/投诉待处理（评分低于3分且未回复的）
        LambdaQueryWrapper<ShopReviewDO> negativeWrapper = new LambdaQueryWrapper<>();
        negativeWrapper.in(ShopReviewDO::getShopId, shopIds)
                .lt(ShopReviewDO::getRating, 3)
                .eq(ShopReviewDO::getStatus, 1)
                .isNull(ShopReviewDO::getReply);
        long negativeComments = shopReviewMapper.selectCount(negativeWrapper);

        // 今日已删除数量 (用于前端提示)
        LambdaQueryWrapper<ShopReviewDO> deletedWrapper = new LambdaQueryWrapper<>();
        deletedWrapper.in(ShopReviewDO::getShopId, shopIds)
                .eq(ShopReviewDO::getStatus, 2)
                .ge(ShopReviewDO::getUpdatedAt, todayStart);
        long todayDeletedCount = shopReviewMapper.selectCount(deletedWrapper);

        dashboard.put("todayNewComments", todayNewComments);
        dashboard.put("todayTrend", todayTrend);
        dashboard.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        dashboard.put("ratingTrend", 0);
        dashboard.put("pendingReply", pendingReply);
        dashboard.put("replyTrend", 0);
        dashboard.put("negativeComments", negativeComments);
        dashboard.put("todayDeletedCount", todayDeletedCount);

        return dashboard;
    }

    @Override
    @Transactional
    public void topComment(Long merchantId, Long commentId, Boolean isTop) {
        log.info("置顶商家评论: merchantId={}, commentId={}, isTop={}", merchantId, commentId, isTop);

        validateMerchant(merchantId);

        ShopReviewDO review = shopReviewMapper.selectById(commentId);
        if (review == null) {
            throw new BusinessException(40404, "评论不存在");
        }

        // 验证评论属于该商家的门店
        ShopDO shop = shopMapper.selectById(review.getShopId());
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException(40300, "无权操作该评论");
        }

        // 这里需要在数据库表中添加is_top字段
        // 暂时记录日志，实际需要修改数据库表结构
        log.info("置顶评论功能需要在数据库表中添加is_top字段");
    }

    private void validateMerchant(Long merchantId) {
        MerchantDO merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(40404, "商家不存在");
        }
        if (merchant.getStatus() != 1) {
            throw new BusinessException(40300, "商家账号已被禁用");
        }
    }

    private void validateShopBelongsToMerchant(Long merchantId, Long shopId) {
        ShopDO shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(40404, "门店不存在");
        }
        if (!merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException(40300, "无权访问该门店");
        }
    }

    private List<Long> getShopIdsByMerchant(Long merchantId) {
        LambdaQueryWrapper<ShopDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopDO::getMerchantId, merchantId);
        List<ShopDO> shops = shopMapper.selectList(wrapper);
        return shops.stream().map(ShopDO::getId).collect(Collectors.toList());
    }

    private PageResult<CommentVO> emptyPageResult(Integer pageNum, Integer pageSize) {
        PageResult<CommentVO> result = new PageResult<>();
        result.setList(new ArrayList<>());
        result.setTotal(0L);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        Map<String, Long> tabCounts = new HashMap<>();
        tabCounts.put("all", 0L);
        tabCounts.put("published", 0L);
        tabCounts.put("deleted", 0L);
        tabCounts.put("negative", 0L);
        result.setTabCounts(tabCounts);
        return result;
    }

    private CommentVO convertToCommentVO(ShopReviewDO review) {
        CommentVO response = new CommentVO();
        response.setId(review.getId());
        response.setContent(review.getContent());
        response.setLikes(review.getLikeCount());
        response.setLiked(false);
        response.setTime(review.getCreatedAt());

        // 获取用户信息
        if (review.getUserId() != null) {
            UserDO user = userMapper.selectById(review.getUserId());
            if (user != null) {
                response.setAuthorId(user.getId());
                response.setAuthor(user.getUsername());

                // 设置头像：如果用户没有头像，使用DefaultAvatar常量类获取默认头像
                String avatar = user.getAvatar();
                if (avatar == null || avatar.trim().isEmpty()) {
                    avatar = DefaultAvatar.getRandomAvatar();
                    log.debug("用户{}没有头像，使用默认头像: {}", user.getId(), avatar);
                }
                response.setAvatar(avatar);
            }
        }

        // 获取门店信息（用于显示关联商家）
        if (review.getShopId() != null) {
            ShopDO shop = shopMapper.selectById(review.getShopId());
            if (shop != null) {
                response.setNoteTitle(shop.getName()); // 使用门店名称
            }
        }

        // 设置状态
        response.setStatus(review.getStatus() == 1 ? "published" : "deleted");

        // 设置评分
        if (review.getRating() != null) {
            response.setRating(review.getRating().doubleValue());
        }
        if (review.getTasteScore() != null) {
            response.setTasteScore(review.getTasteScore().doubleValue());
        }
        if (review.getEnvironmentScore() != null) {
            response.setEnvironmentScore(review.getEnvironmentScore().doubleValue());
        }
        if (review.getServiceScore() != null) {
            response.setServiceScore(review.getServiceScore().doubleValue());
        }

        // 设置图片
        if (StringUtils.hasText(review.getImages())) {
            try {
                // 假设images字段存储的是JSON数组格式
                response.setImages(Arrays.asList(review.getImages().split(",")));
            } catch (Exception e) {
                log.warn("解析评论图片失败: reviewId={}", review.getId(), e);
            }
        }

        // 设置商家回复
        if (StringUtils.hasText(review.getReply())) {
            response.setReply(review.getReply());
            response.setReplyTime(review.getReplyTime());
        }

        return response;
    }

    @Override
    public void exportComments(Long merchantId, jakarta.servlet.http.HttpServletResponse response, Long shopId,
            Integer status, String keyword) {
        log.info("导出评论数据: merchantId={}, shopId={}", merchantId, shopId);

        validateMerchant(merchantId);

        // 获取门店ID列表
        List<Long> shopIds;
        if (shopId != null) {
            validateShopBelongsToMerchant(merchantId, shopId);
            shopIds = Collections.singletonList(shopId);
        } else {
            shopIds = getShopIdsByMerchant(merchantId);
        }

        if (shopIds.isEmpty()) {
            throw new BusinessException(40404, "暂无数据可导出");
        }

        // 构建查询
        LambdaQueryWrapper<ShopReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ShopReviewDO::getShopId, shopIds);
        if (status != null) {
            wrapper.eq(ShopReviewDO::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ShopReviewDO::getContent, keyword);
        }
        wrapper.orderByDesc(ShopReviewDO::getCreatedAt);

        List<ShopReviewDO> reviews = shopReviewMapper.selectList(wrapper);

        // 转换为导出数据
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ShopReviewDO review : reviews) {
            Map<String, Object> row = new LinkedHashMap<>();
            // 用户信息
            String userName = "匿名用户";
            if (review.getUserId() != null) {
                UserDO user = userMapper.selectById(review.getUserId());
                if (user != null)
                    userName = user.getUsername();
            }
            row.put("用户名", userName);

            // 评分
            row.put("综合评分", review.getRating());
            row.put("口味", review.getTasteScore());
            row.put("环境", review.getEnvironmentScore());
            row.put("服务", review.getServiceScore());

            // 内容
            row.put("评论内容", review.getContent());
            row.put("评论时间", review.getCreatedAt().toString().replace("T", " "));

            // 门店
            ShopDO shop = shopMapper.selectById(review.getShopId());
            row.put("所属门店", shop != null ? shop.getName() : "未知门店");

            // 状态
            String statusStr = "未知";
            if (review.getStatus() == 1)
                statusStr = "正常显示";
            else if (review.getStatus() == 2)
                statusStr = "已删除";
            else if (review.getStatus() == 0)
                statusStr = "待审核";
            row.put("状态", statusStr);

            // 回复
            row.put("商家回复", review.getReply() != null ? review.getReply() : "");
            row.put("回复时间", review.getReplyTime() != null ? review.getReplyTime().toString().replace("T", " ") : "");

            rows.add(row);
        }

        // 导出Excel
        try {
            cn.hutool.poi.excel.ExcelWriter writer = cn.hutool.poi.excel.ExcelUtil.getWriter(true);
            writer.write(rows, true);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            String fileName = java.net.URLEncoder.encode("评论数据导出", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            jakarta.servlet.ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
            cn.hutool.core.io.IoUtil.close(out); // Using fully qualified IoUtil if import conflicts, but assuming
                                                 // Hutool
            // IoUtil
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new BusinessException(50000, "导出失败");
        }
    }
}
