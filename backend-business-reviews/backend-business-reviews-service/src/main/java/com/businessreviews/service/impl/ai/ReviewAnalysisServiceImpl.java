package com.businessreviews.service.impl.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.ShopMapper;
import com.businessreviews.mapper.ShopReviewMapper;
import com.businessreviews.model.dataobject.ShopDO;
import com.businessreviews.model.dataobject.ShopReviewDO;
import com.businessreviews.model.dto.ai.WeeklyReportDTO;
import com.businessreviews.service.ai.ReviewAnalysisAgent;
import com.businessreviews.service.ai.ReviewAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论分析服务实现类
 * 负责从数据库获取评论，调用 AI 代理进行分析，并返回结构化结果
 * 
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAnalysisServiceImpl implements ReviewAnalysisService {

    private final ReviewAnalysisAgent reviewAnalysisAgent;
    private final ShopReviewMapper shopReviewMapper;
    private final ShopMapper shopMapper;

    /**
     * 最大处理评论数量，防止 Token 溢出
     */
    private static final int MAX_REVIEW_COUNT = 100;

    /**
     * 最大字符数限制
     */
    private static final int MAX_CHAR_LIMIT = 5000;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成商家周报
     * <p>
     * 统计过去7天内的评论数据，并调用AI进行情感分析和建议生成。
     * </p>
     *
     * @param shopId 门店ID
     * @return 周报数据DTO
     */
    @Override
    public WeeklyReportDTO generateReport(Long shopId) {
        log.info("开始生成商家周报, shopId: {}", shopId);

        // 1. 查询商家信息
        ShopDO shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(404, "商家不存在");
        }

        String shopName = shop.getName();
        log.debug("商家名称: {}", shopName);

        // 2. 计算时间范围（过去7天）
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(7);
        String period = startTime.format(DATE_FORMATTER) + " ~ " + endTime.format(DATE_FORMATTER);

        // 3. 查询过去7天的评论
        LambdaQueryWrapper<ShopReviewDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopReviewDO::getShopId, shopId)
                .eq(ShopReviewDO::getStatus, 1) // 只查询正常状态的评论
                .ge(ShopReviewDO::getCreatedAt, startTime)
                .le(ShopReviewDO::getCreatedAt, endTime)
                .orderByDesc(ShopReviewDO::getCreatedAt)
                .last("LIMIT " + MAX_REVIEW_COUNT); // 限制最大数量

        List<ShopReviewDO> reviews = shopReviewMapper.selectList(queryWrapper);
        log.info("查询到 {} 条评论", reviews.size());

        // 4. 如果没有评论，返回默认报告
        if (reviews.isEmpty()) {
            return buildEmptyReport(shopName, period);
        }

        // 5. 过滤并拼接评论内容
        String reviewsText = buildReviewsText(reviews);
        log.debug("拼接后的评论长度: {} 字符", reviewsText.length());

        // 6. 调用 AI 代理分析评论
        String analysisResult;
        try {
            log.info("调用 AI 代理分析评论...");
            analysisResult = reviewAnalysisAgent.analyzeReviews(shopName, reviewsText);
            log.debug("AI 分析结果: {}", analysisResult);
        } catch (Exception e) {
            log.error("AI 分析失败", e);
            throw new BusinessException(500, "AI 分析服务暂时不可用，请稍后重试");
        }

        // 7. 解析 JSON 结果并构建 DTO
        WeeklyReportDTO report = parseAnalysisResult(analysisResult);
        report.setReviewCount(reviews.size());
        report.setGeneratedAt(LocalDateTime.now().format(DATETIME_FORMATTER));
        report.setPeriod(period);

        log.info("周报生成完成, shopId: {}, sentimentScore: {}", shopId, report.getSentimentScore());
        return report;
    }

    /**
     * 构建评论文本
     * 过滤空评论，拼接成字符串，限制最大长度
     */
    private String buildReviewsText(List<ShopReviewDO> reviews) {
        StringBuilder sb = new StringBuilder();
        int index = 1;

        for (ShopReviewDO review : reviews) {
            String content = review.getContent();

            // 跳过空评论
            if (content == null || content.trim().isEmpty()) {
                continue;
            }

            String reviewLine = String.format("%d. [评分%.1f] %s\n",
                    index++,
                    review.getRating() != null ? review.getRating().doubleValue() : 0.0,
                    content.trim());

            // 检查是否超过最大字符限制
            if (sb.length() + reviewLine.length() > MAX_CHAR_LIMIT) {
                log.debug("达到字符限制 {}，停止拼接", MAX_CHAR_LIMIT);
                break;
            }

            sb.append(reviewLine);
        }

        return sb.toString();
    }

    /**
     * 解析 AI 分析结果
     */
    private WeeklyReportDTO parseAnalysisResult(String analysisResult) {
        try {
            // 尝试提取 JSON 内容（处理可能的 markdown 代码块）
            String jsonStr = extractJsonFromResponse(analysisResult);
            JSONObject json = JSON.parseObject(jsonStr);

            return WeeklyReportDTO.builder()
                    .sentimentScore(json.getInteger("sentimentScore"))
                    .summary(json.getString("summary"))
                    .pros(json.getList("pros", String.class))
                    .cons(json.getList("cons", String.class))
                    .advice(json.getString("advice"))
                    .build();
        } catch (Exception e) {
            log.error("解析 AI 结果失败: {}", analysisResult, e);
            // 返回一个默认结果而不是抛出异常
            return WeeklyReportDTO.builder()
                    .sentimentScore(5)
                    .summary("AI 分析结果解析失败，请重试")
                    .pros(new ArrayList<>())
                    .cons(new ArrayList<>())
                    .advice("请稍后重试或联系技术支持")
                    .build();
        }
    }

    /**
     * 从响应中提取 JSON（处理 markdown 代码块情况）
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "{}";
        }

        // 尝试提取 ```json ... ``` 中的内容
        String trimmed = response.trim();
        if (trimmed.startsWith("```")) {
            int startIndex = trimmed.indexOf('\n') + 1;
            int endIndex = trimmed.lastIndexOf("```");
            if (startIndex > 0 && endIndex > startIndex) {
                return trimmed.substring(startIndex, endIndex).trim();
            }
        }

        // 直接尝试找到 JSON 对象
        int startIndex = trimmed.indexOf('{');
        int endIndex = trimmed.lastIndexOf('}');
        if (startIndex >= 0 && endIndex > startIndex) {
            return trimmed.substring(startIndex, endIndex + 1);
        }

        return trimmed;
    }

    /**
     * 构建空报告（没有评论时使用）
     */
    private WeeklyReportDTO buildEmptyReport(String shopName, String period) {
        return WeeklyReportDTO.builder()
                .sentimentScore(5)
                .summary("本周暂无用户评论")
                .pros(new ArrayList<>())
                .cons(new ArrayList<>())
                .advice("建议通过优惠活动、社交媒体等方式吸引更多用户到店体验并留下评价")
                .reviewCount(0)
                .generatedAt(LocalDateTime.now().format(DATETIME_FORMATTER))
                .period(period)
                .build();
    }
}
