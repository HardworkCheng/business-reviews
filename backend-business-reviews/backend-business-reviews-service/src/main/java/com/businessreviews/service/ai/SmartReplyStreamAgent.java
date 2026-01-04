package com.businessreviews.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * 全场景智能回复 AI 代理接口 (流式版)
 * 返回 TokenStream 实现逐字输出效果
 * 
 * @author businessreviews
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "deepSeekChatModel")
public interface SmartReplyStreamAgent {

    /**
     * 智能生成评论回复（流式）
     * 
     * @param reviewText           用户的评论内容
     * @param compensationStrategy 商家的赠礼/优惠策略（可选）
     * @return TokenStream 用于流式输出
     */
    @SystemMessage("""
            你是一位高情商的校园商圈商家运营经理，擅长与大学生顾客沟通。

            【核心任务】
            请根据用户评论生成一段得体的商家回复。

            【思维链】
            第一步：判断评论是好评还是差评
            第二步：选择合适的回复策略

            ★ 好评：开心、感激、俏皮，用emoji如🥰❤️🎉，赠礼作为"惊喜回馈"
            ★ 差评：诚恳、严肃、抱歉，用emoji如😔🙏，赠礼作为"表达歉意/补偿"

            【约束】
            1. 回复字数控制在 100-150 字
            2. 使用亲切称呼（亲、同学）
            3. 如果没有赠礼策略，不要编造优惠承诺
            4. 直接输出回复内容，不要输出分析过程
            """)
    @UserMessage("""
            用户评论内容：
            {{reviewText}}

            商家赠礼策略：{{compensationStrategy}}

            请生成合适的回复。
            """)
    TokenStream generateSmartReplyStream(
            @V("reviewText") String reviewText,
            @V("compensationStrategy") String compensationStrategy);
}
