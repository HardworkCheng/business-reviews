package com.businessreviews.service.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * 评论分析 AI 代理接口
 * 使用 LangChain4j 的 @AiService 注解自动生成实现
 * 
 * 注意：使用 DeepSeek 模型（商家端）
 * 
 * @author businessreviews
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "deepSeekChatModel")
public interface ReviewAnalysisAgent {

    /**
     * 分析商家评论并生成周报
     * 
     * @param shopName 商家名称
     * @param reviews  评论内容（拼接后的字符串）
     * @return JSON 格式的分析结果
     */
    @SystemMessage("""
            你是一位专业的餐饮商圈数据分析师，擅长从用户评论中提取关键信息并生成有价值的商业洞察。

            你的任务是分析商家的用户评论，并生成一份结构化的周报。请严格按照以下JSON格式输出，不要添加任何其他内容：

            {
                "sentimentScore": <0-10的整数，表示整体情感倾向，0为极度负面，10为极度正面>,
                "summary": "<50字以内的总体摘要>",
                "pros": ["<优点1>", "<优点2>", "<优点3>"],
                "cons": ["<缺点1>", "<缺点2>", "<缺点3>"],
                "advice": "<给商家的改进建议，100字以内>"
            }

            分析要点：
            1. sentimentScore 应综合考虑所有评论的正负面情感
            2. summary 需简洁概括本周评论的整体特点
            3. pros 从评论中提取最多3个突出优点，如没有可少于3个
            4. cons 从评论中提取最多3个主要问题，如没有可少于3个
            5. advice 应针对cons给出具体可执行的改进建议

            注意：
            - 必须严格输出纯JSON格式，不要有其他文字
            - 如果评论数量较少或内容单一，据实分析即可
            - 保持客观专业的分析态度
            """)
    @UserMessage("""
            商家名称：{{shopName}}

            以下是过去7天收到的用户评论：

            {{reviews}}

            请分析以上评论并生成周报。
            """)
    String analyzeReviews(@V("shopName") String shopName, @V("reviews") String reviews);
}
