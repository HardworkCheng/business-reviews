package com.businessreviews.service.impl.ai;

import com.businessreviews.service.ai.ReviewReplyAgent;
import com.businessreviews.service.ai.ReviewReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 差评智能回复服务实现
 * 
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {

    private final ReviewReplyAgent reviewReplyAgent;

    @Override
    public String generateReply(String reviewText, String strategy) {
        // 参数校验
        if (!StringUtils.hasText(reviewText)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }

        log.info("开始生成差评回复，评论内容: {}, 补偿策略: {}",
                reviewText.length() > 50 ? reviewText.substring(0, 50) + "..." : reviewText,
                strategy);

        try {
            // 处理补偿策略，如果为空则明确告知AI不需要提供补偿
            String compensationStrategy = StringUtils.hasText(strategy)
                    ? strategy
                    : "无（仅诚恳道歉，不提供任何优惠补偿）";

            // 调用 AI 生成回复
            String reply = reviewReplyAgent.generateReply(reviewText, compensationStrategy);

            log.info("差评回复生成成功，回复长度: {} 字符", reply.length());
            return reply;

        } catch (Exception e) {
            log.error("AI生成回复失败", e);
            // 返回一个保底的默认回复
            return generateDefaultReply(reviewText, strategy);
        }
    }

    /**
     * 生成默认回复（AI调用失败时的兜底方案）
     */
    private String generateDefaultReply(String reviewText, String strategy) {
        StringBuilder reply = new StringBuilder();
        reply.append("亲爱的同学，真的非常抱歉给您带来了不好的体验！😢 ");
        reply.append("我们非常重视您的反馈，已经把您提到的问题记录下来了，会尽快改进。");

        if (StringUtils.hasText(strategy)) {
            reply.append(" 为表歉意，").append(strategy).append("，希望您能接受我们的心意。");
        }

        reply.append(" 真心希望您能再给我们一次机会！🙏");

        return reply.toString();
    }
}
