package com.businessreviews.service.impl.ai;

import com.businessreviews.service.ai.SmartReplyAgent;
import com.businessreviews.service.ai.ReviewReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 全场景智能回复服务实现
 * 
 * 功能：自动识别用户评论情感倾向（好评/差评），生成相应风格的回复
 * - 好评：热情、感激、俏皮，重在建立连接和鼓励复购
 * - 差评：诚恳、安抚、专业，重在安抚情绪和挽回流失
 * 
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {

    private final SmartReplyAgent smartReplyAgent;

    @Override
    public String generateReply(String reviewText, String strategy) {
        // 参数校验
        if (!StringUtils.hasText(reviewText)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        log.info("开始智能生成回复，评论内容: {}, 赠礼策略: {}",
                reviewText.length() > 50 ? reviewText.substring(0, 50) + "..." : reviewText,
                strategy);

        try {
            // 处理赠礼策略
            // 不再硬编码"道歉"相关文案，完全交给AI根据情感判断动态生成
            String giftStrategy = StringUtils.hasText(strategy)
                    ? strategy
                    : "无（不提供任何赠品或优惠）";

            // 调用智能回复 AI 生成回复
            String reply = smartReplyAgent.generateSmartReply(reviewText, giftStrategy);

            log.info("智能回复生成成功，回复长度: {} 字符", reply.length());
            return reply;

        } catch (Exception e) {
            log.error("AI生成回复失败", e);
            // 返回一个通用的保底默认回复
            return generateDefaultReply(strategy);
        }
    }

    /**
     * 生成默认回复（AI调用失败时的兜底方案）
     * 使用中性语气，适用于好评和差评
     */
    private String generateDefaultReply(String strategy) {
        StringBuilder reply = new StringBuilder();
        reply.append("亲爱的同学，感谢您的评价！😊 ");
        reply.append("您的反馈对我们非常重要，我们会认真对待每一条意见。");

        if (StringUtils.hasText(strategy)) {
            reply.append(" 这是送给您的小心意：").append(strategy).append("，希望您喜欢！");
        }

        reply.append(" 期待您的再次光临！❤️");

        return reply.toString();
    }
}
