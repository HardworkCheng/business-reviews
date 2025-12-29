package com.businessreviews.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * AI模型配置类
 * 
 * 配置多个ChatLanguageModel Bean，用于不同的业务场景：
 * - deepSeekChatModel: DeepSeek模型，用于商家端评论分析和智能回复
 * - visionChatModel: 通义千问Qwen-VL模型，用于用户端探店笔记AI生成（支持图片识别）
 * 
 * @author businessreviews
 */
@Slf4j
@Configuration
public class AiModelConfig {

    // ============================================================
    // DeepSeek 模型配置（商家端：评论分析、智能回复）
    // ============================================================

    @Value("${ai.deepseek.api-key}")
    private String deepSeekApiKey;

    @Value("${ai.deepseek.base-url}")
    private String deepSeekBaseUrl;

    @Value("${ai.deepseek.model-name}")
    private String deepSeekModelName;

    @Value("${ai.deepseek.temperature}")
    private Double deepSeekTemperature;

    @Value("${ai.deepseek.max-tokens}")
    private Integer deepSeekMaxTokens;

    @Value("${ai.deepseek.timeout}")
    private Integer deepSeekTimeout;

    // ============================================================
    // 通义千问 Qwen-VL 模型配置（用户端：探店笔记AI生成）
    // ============================================================

    @Value("${ai.qwen-vision.api-key}")
    private String qwenVisionApiKey;

    @Value("${ai.qwen-vision.base-url}")
    private String qwenVisionBaseUrl;

    @Value("${ai.qwen-vision.model-name}")
    private String qwenVisionModelName;

    @Value("${ai.qwen-vision.temperature}")
    private Double qwenVisionTemperature;

    @Value("${ai.qwen-vision.max-tokens}")
    private Integer qwenVisionMaxTokens;

    @Value("${ai.qwen-vision.timeout}")
    private Integer qwenVisionTimeout;

    /**
     * DeepSeek 聊天模型 Bean
     * 
     * 用于商家端的AI功能：
     * - 评论分析（ReviewAnalysisAgent）
     * - 智能回复（SmartReplyAgent）
     * 
     * @Primary 标注为默认的 ChatLanguageModel，@AiService 注解的类会自动使用此模型
     */
    @Bean
    @Primary
    public ChatLanguageModel deepSeekChatModel() {
        log.info("初始化 DeepSeek 聊天模型: {}", deepSeekModelName);

        return OpenAiChatModel.builder()
                .apiKey(deepSeekApiKey)
                .baseUrl(deepSeekBaseUrl)
                .modelName(deepSeekModelName)
                .temperature(deepSeekTemperature)
                .maxTokens(deepSeekMaxTokens)
                .timeout(Duration.ofSeconds(deepSeekTimeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 通义千问 Qwen-VL 视觉模型 Bean
     * 
     * 用于用户端的AI功能：
     * - 探店笔记AI智能生成（VisionNoteService）
     * 
     * 此模型支持图片识别，可以处理多模态输入（文本+图片）
     */
    @Bean("visionChatModel")
    public ChatLanguageModel visionChatModel() {
        log.info("初始化 通义千问 Qwen-VL 视觉模型: {}", qwenVisionModelName);

        return OpenAiChatModel.builder()
                .apiKey(qwenVisionApiKey)
                .baseUrl(qwenVisionBaseUrl)
                .modelName(qwenVisionModelName)
                .temperature(qwenVisionTemperature)
                .maxTokens(qwenVisionMaxTokens)
                .timeout(Duration.ofSeconds(qwenVisionTimeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
