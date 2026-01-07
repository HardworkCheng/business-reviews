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
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 商家评论服务实现类
 * <p>
 * 处理商家端的评价管理业务。
 * 核心功能包括：
 * 1. 评论列表查询（支持状态、关键词、差评筛选）
 * 2. 评论回复与删除（包含每日删除限额控制）
 * 3. 评论数据统计（总数、待回复、差评数）
 * 4. 评价看板数据计算（今日新增、评分趋势、待处理事项）
 * 5. 评论导出（Excel格式）
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantCommentServiceImpl implements MerchantCommentService {

    private final ShopReviewMapper shopReviewMapper;
    private final ShopMapper shopMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    /**
     * 获取评论列表
     * <p>
     * 支持多维度筛选：门店、状态、关键词、是否差评。
     * 自动计算各状态Tab的统计数量。
     * </p>
     *
     * @param merchantId 商家ID
     * @param shopId     门店ID（可选，若为空则查询商家所有门店）
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param status     状态（1:正常, 2:删除, 0:待审核）
     * @param keyword    搜索关键词
     * @param isNegative 是否仅查看差评（评分<=2）
     * @return 评论VO分页列表（包含Tab计数）
     */
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

        // 根据isNegative参数添加评分筛选条件
        if (isNegative != null) {
            if (isNegative) {
                // 差评：评分 <= 2星
                wrapper.le(ShopReviewDO::getRating, BigDecimal.valueOf(2));
            } else {
                // 正常评论：评分 >= 3星
                wrapper.ge(ShopReviewDO::getRating, BigDecimal.valueOf(3));
            }
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(ShopReviewDO::getContent, keyword);
        }
        wrapper.orderByDesc(ShopReviewDO::getCreatedAt);

        Page<ShopReviewDO> page = new Page<>(pageNum, pageSize);
        Page<ShopReviewDO> reviewPage = shopReviewMapper.selectPage(page, wrapper);
        List<ShopReviewDO> reviews = reviewPage.getRecords();

        // ✅ 优化：批量预加载用户和门店信息，消除 N+1 查询问题
        List<CommentVO> list = convertToCommentVOList(reviews);

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

        // 正常显示（3星及以上的评论）
        LambdaQueryWrapper<ShopReviewDO> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.in(ShopReviewDO::getShopId, shopIds)
                .eq(ShopReviewDO::getStatus, 1)
                .ge(ShopReviewDO::getRating, BigDecimal.valueOf(3)); // 只统计3星及以上
        long published = shopReviewMapper.selectCount(publishedWrapper);

        // 已删除
        LambdaQueryWrapper<ShopReviewDO> deletedWrapper = new LambdaQueryWrapper<>();
        deletedWrapper.in(ShopReviewDO::getShopId, shopIds)
                .eq(ShopReviewDO::getStatus, 2);
        long deleted = shopReviewMapper.selectCount(deletedWrapper);

        // 差评/投诉（评分 <= 2星）
        LambdaQueryWrapper<ShopReviewDO> negativeWrapper = new LambdaQueryWrapper<>();
        negativeWrapper.in(ShopReviewDO::getShopId, shopIds)
                .le(ShopReviewDO::getRating, BigDecimal.valueOf(2)) // 修改为 <= 2星
                .eq(ShopReviewDO::getStatus, 1);
        long negative = shopReviewMapper.selectCount(negativeWrapper);

        counts.put("all", all);
        counts.put("published", published);
        counts.put("deleted", deleted);
        counts.put("negative", negative);

        return counts;
    }

    /**
     * 回复评论
     * <p>
     * 商家对用户评论进行回复。
     * 只能回复属于自己门店的评论。
     * </p>
     *
     * @param merchantId 商家ID
     * @param operatorId 操作人ID
     * @param commentId  评论ID
     * @param content    回复内容
     * @throws BusinessException 如果评论不存在(40404)或无权操作(40300)
     */
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

    /**
     * 删除评论
     * <p>
     * 软删除评论（状态置为2）。
     * 包含安全控制：每日最多只能删除2条评论。
     * 删除后会重新计算门店评分。
     * </p>
     *
     * @param merchantId 商家ID
     * @param operatorId 操作人ID
     * @param commentId  评论ID
     * @throws BusinessException 如果超过每日限额或无权操作
     */
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

        // 重新计算门店评分
        recalculateShopRatings(review.getShopId());
    }

    /**
     * 获取评论基础统计
     * <p>
     * 统计总评论数、待回复数等。
     * </p>
     *
     * @param merchantId 商家ID
     * @return 统计数据Map
     */
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

    /**
     * 获取评价看板数据
     * <p>
     * 提供数据概览，包括：
     * 1. 今日新增评论及趋势
     * 2. 平均评分
     * 3. 待处理事项（待回复、差评）
     * </p>
     *
     * @param merchantId 商家ID
     * @param shopId     门店ID（可选）
     * @return 看板数据Map
     */
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

    /**
     * 置顶/取消置顶评论
     * <p>
     * 将优质评论置顶显示。
     * 目前仅记录日志，需数据库字段支持。
     * </p>
     *
     * @param merchantId 商家ID
     * @param commentId  评论ID
     * @param isTop      是否置顶
     */
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

    /**
     * 批量转换评论列表为 VO
     * <p>
     * 使用 In-Memory Map Assembly 模式解决 N+1 查询问题
     * </p>
     *
     * @param reviews 评论列表
     * @return CommentVO 列表
     */
    private List<CommentVO> convertToCommentVOList(List<ShopReviewDO> reviews) {
        if (reviews.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集所有用户ID和门店ID
        Set<Long> userIds = reviews.stream()
                .map(ShopReviewDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> shopIds = reviews.stream()
                .map(ShopReviewDO::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, UserDO> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<UserDO> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(UserDO::getId, u -> u, (a, b) -> a));
        }

        // 批量查询门店信息
        Map<Long, ShopDO> shopMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            List<ShopDO> shops = shopMapper.selectBatchIds(shopIds);
            shopMap = shops.stream()
                    .collect(Collectors.toMap(ShopDO::getId, s -> s, (a, b) -> a));
        }

        // 内存组装 VO
        final Map<Long, UserDO> finalUserMap = userMap;
        final Map<Long, ShopDO> finalShopMap = shopMap;

        return reviews.stream()
                .map(review -> convertToCommentVO(review, finalUserMap, finalShopMap))
                .collect(Collectors.toList());
    }

    /**
     * 转换单个评论为 VO（从预加载的 Map 中获取关联数据）
     *
     * @param review  评论实体
     * @param userMap 用户信息 Map
     * @param shopMap 门店信息 Map
     * @return CommentVO
     */
    private CommentVO convertToCommentVO(ShopReviewDO review, Map<Long, UserDO> userMap, Map<Long, ShopDO> shopMap) {
        CommentVO response = new CommentVO();
        response.setId(review.getId());
        response.setContent(review.getContent());
        response.setLikes(review.getLikeCount());
        response.setLiked(false);
        response.setTime(review.getCreatedAt());

        // ✅ 优化：从预加载的 Map 中获取用户信息，避免 N+1 查询
        if (review.getUserId() != null) {
            UserDO user = userMap.get(review.getUserId());
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

        // ✅ 优化：从预加载的 Map 中获取门店信息，避免 N+1 查询
        if (review.getShopId() != null) {
            ShopDO shop = shopMap.get(review.getShopId());
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

    /**
     * 导出评论数据
     * <p>
     * 根据筛选条件将评论数据导出为Excel文件。
     * 使用Hutool工具类生成Excel。
     * </p>
     *
     * @param merchantId 商家ID
     * @param response   HTTP响应对象
     * @param shopId     门店ID
     * @param status     状态
     * @param keyword    关键词
     */
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

        // ✅ 优化：批量预加载用户和门店信息，消除导出时的 N+1 查询问题
        Set<Long> userIds = reviews.stream()
                .map(ShopReviewDO::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> exportShopIds = reviews.stream()
                .map(ShopReviewDO::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, UserDO> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<UserDO> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(UserDO::getId, u -> u, (a, b) -> a));
        }

        // 批量查询门店信息
        Map<Long, ShopDO> shopMap = new HashMap<>();
        if (!exportShopIds.isEmpty()) {
            List<ShopDO> shops = shopMapper.selectBatchIds(exportShopIds);
            shopMap = shops.stream()
                    .collect(Collectors.toMap(ShopDO::getId, s -> s, (a, b) -> a));
        }

        // 转换为导出数据（使用 Map 避免逐条查询）
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ShopReviewDO review : reviews) {
            Map<String, Object> row = new LinkedHashMap<>();

            // ✅ 优化：从 Map 中获取用户信息
            String userName = "匿名用户";
            if (review.getUserId() != null) {
                UserDO user = userMap.get(review.getUserId());
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

            // ✅ 优化：从 Map 中获取门店信息
            ShopDO shop = shopMap.get(review.getShopId());
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

    /**
     * 重新计算门店评分
     */
    private void recalculateShopRatings(Long shopId) {
        // 查询该门店所有正常显示的评论
        LambdaQueryWrapper<ShopReviewDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopReviewDO::getShopId, shopId)
                .eq(ShopReviewDO::getStatus, 1); // Only active reviews
        List<ShopReviewDO> reviews = shopReviewMapper.selectList(queryWrapper);

        if (reviews.isEmpty()) {
            // 如果没有评论，重置为默认高分（5.0）
            ShopDO shopUpdate = new ShopDO();
            shopUpdate.setId(shopId);
            shopUpdate.setRating(BigDecimal.valueOf(5.0));
            shopUpdate.setTasteScore(BigDecimal.valueOf(5.0));
            shopUpdate.setEnvironmentScore(BigDecimal.valueOf(5.0));
            shopUpdate.setServiceScore(BigDecimal.valueOf(5.0));
            shopUpdate.setReviewCount(0);
            shopMapper.updateById(shopUpdate);
            log.info("门店无有效评论，重置评分: shopId={}", shopId);
            return;
        }

        // 计算平均分
        double avgRating = reviews.stream().mapToDouble(r -> r.getRating().doubleValue()).average().orElse(5.0);
        double avgTaste = reviews.stream().mapToDouble(r -> r.getTasteScore().doubleValue()).average().orElse(5.0);
        double avgEnv = reviews.stream().mapToDouble(r -> r.getEnvironmentScore().doubleValue()).average().orElse(5.0);
        double avgService = reviews.stream().mapToDouble(r -> r.getServiceScore().doubleValue()).average().orElse(5.0);

        ShopDO shopUpdate = new ShopDO();
        shopUpdate.setId(shopId);
        shopUpdate.setRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
        shopUpdate.setTasteScore(BigDecimal.valueOf(avgTaste).setScale(2, RoundingMode.HALF_UP));
        shopUpdate.setEnvironmentScore(BigDecimal.valueOf(avgEnv).setScale(2, RoundingMode.HALF_UP));
        shopUpdate.setServiceScore(BigDecimal.valueOf(avgService).setScale(2, RoundingMode.HALF_UP));
        shopUpdate.setReviewCount(reviews.size());

        shopMapper.updateById(shopUpdate);
        log.info("重新计算门店评分完成: shopId={}, rating={}, count={}", shopId, avgRating, reviews.size());
    }
}
