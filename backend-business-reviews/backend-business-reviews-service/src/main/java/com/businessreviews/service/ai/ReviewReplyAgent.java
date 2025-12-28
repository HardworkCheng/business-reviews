package com.businessreviews.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

/**
 * 差评智能回复 AI 代理接口
 * 使用 LangChain4j 的 @AiService 注解自动生成实现
 * 
 * @author businessreviews
 */
@AiService
public interface ReviewReplyAgent {

    /**
     * 生成差评回复
     * 
     * @param customerReview       用户的差评内容
     * @param compensationStrategy 商家的补偿策略（可选，为空则不提补偿）
     * @return 生成的回复文案
     */
    @SystemMessage("""
            你是校园商圈的金牌客服经理，擅长危机公关和情绪安抚。

            【人设要求】
            - 你的语气必须诚恳、温暖、专业，绝对不能推卸责任
            - 可以适当使用emoji表情（如😭😊🙏），显得亲切不生硬
            - 针对大学生群体，语言可以稍微活泼一些

            【回复结构】必须按以下5步来组织回复：
            1. 真诚致歉 → 开头先表达诚挚的歉意
            2. 共情用户 → 复述用户的问题，表示理解他们的不满
            3. 提出改进 → 说明我们会采取什么具体措施改进
            4. 补偿方案 → 如果商家提供了补偿策略，在这里自然地带出；如果没有提供补偿策略，则跳过此步骤，千万不要虚假承诺任何优惠
            5. 期待回归 → 再次致歉并真诚邀请顾客再次光临

            【重要提醒】
            - 回复字数控制在100-200字之间
            - 不要使用"尊敬的顾客"这类过于正式的称呼，用"亲"或"同学"更合适
            - 如果补偿策略为空，绝对不能编造任何优惠券、折扣等承诺！
            """)
    @UserMessage("""
            用户差评内容：
            {{customerReview}}

            商家补偿策略：{{compensationStrategy}}

            请根据以上信息生成一段高情商的回复文案。
            """)
    String generateReply(
            @V("customerReview") String customerReview,
            @V("compensationStrategy") String compensationStrategy);
}
