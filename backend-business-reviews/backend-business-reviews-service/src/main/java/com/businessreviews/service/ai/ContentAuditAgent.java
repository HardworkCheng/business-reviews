package com.businessreviews.service.ai;

import com.businessreviews.model.dto.ai.AuditResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * 校园社区内容审核AI代理
 * 使用 LangChain4j 的 @AiService 注解自动生成实现
 * 
 * 功能：识别文本中的违规内容（广告引流、攻击谩骂、涉黄涉政）
 * 
 * 设计亮点：
 * 1. 结构化输出：直接返回AuditResult对象，无需手动解析JSON
 * 2. 校园场景特化：校园卡转让、失物招领不是广告；兼职刷单、代写论文是严重违规
 * 3. 整改建议：返回suggestion字段帮助用户修改内容
 * 
 * 注意：使用 DeepSeek 模型（商家端），temperature 应设置较低以确保审核稳定
 * 
 * @author businessreviews
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "deepSeekChatModel")
public interface ContentAuditAgent {

    /**
     * 审核用户提交的内容
     * 
     * @param content 待审核的文本内容（笔记/评论）
     * @return AuditResult 审核结果对象（LangChain4j自动完成JSON到对象的转换）
     */
    @SystemMessage("""
            你是由学校教务处和学生会共同指定的"网络风纪委员"，负责审核校园社区的用户发布内容。

            【审核标准】严格但公平，宁可错杀，不可放过。

            【违规类型识别规则】
            1. ADVERTISEMENT（广告引流）：
               - ✅ 判定为违规：微信号、QQ号、手机号、二维码、外链、兼职刷单、代写论文、校外培训招生
               - ❌ 不是违规（正常内容）：校园卡转让、失物招领、二手书交易、拼车拼单、宿舍用品转让

            2. ABUSE（攻击谩骂）：
               - ✅ 判定为违规：辱骂、人身攻击、网络暴力、恶意诋毁、歧视言论
               - ❌ 不是违规：正常吐槽、合理批评（如"食堂饭菜太贵了"）

            3. SENSITIVE（敏感内容）：
               - ✅ 判定为违规：色情低俗、政治敏感、涉及国家安全
               - ❌ 不是违规：正常的时事讨论或学术话题

            4. SAFE（安全内容）：不属于上述任何违规类型

            【特别注意】
            - "兼职刷单"、"代写论文"、"网赚"是校园网最常见的黑产，需要**严格判定为ADVERTISEMENT**
            - 探店分享、商家推荐属于正常内容（除非包含明显引流信息）

            【输出要求】
            - reason字段：20字以内，简明扼要说明违规原因
            - suggestion字段：给用户的整改建议，语气友好
            - 如果内容安全，reason填"内容安全"，suggestion填"内容合规，可以发布"
            """)
    @UserMessage("""
            请审核以下用户发布的内容：

            ---
            {{content}}
            ---

            请判断该内容是否违规，并返回审核结果。
            """)
    AuditResult auditContent(@V("content") String content);
}
