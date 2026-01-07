package com.businessreviews.service.impl.ai;

import com.businessreviews.model.dto.ai.NoteGenerateRequest;
import com.businessreviews.service.ai.VisionNoteStreamService;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 智能探店笔记流式生成服务实现
 * <p>
 * 使用 LangChain4j 的 StreamingChatLanguageModel 实现流式输出，
 * 通过 SSE（Server-Sent Events）将生成的内容逐字推送给前端。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
public class VisionNoteStreamServiceImpl implements VisionNoteStreamService {

    private final StreamingChatLanguageModel visionStreamingChatModel;

    /**
     * 构造函数注入流式视觉模型
     * 使用 @Qualifier 指定注入 visionStreamingChatModel（通义千问 Qwen-VL 流式版）
     */
    public VisionNoteStreamServiceImpl(
            @Qualifier("visionStreamingChatModel") StreamingChatLanguageModel visionStreamingChatModel) {
        this.visionStreamingChatModel = visionStreamingChatModel;
    }

    /**
     * 系统提示词：设定AI人设和写作风格
     */
    private static final String SYSTEM_PROMPT = """
            你是一位眼光毒辣、语气夸张的大学生探店博主，在小红书拥有10万粉丝。
            你具备视觉识别能力，请仔细观察图片细节（食物光泽、摆盘、环境灯光、分量大小）。

            【写作风格要求】
            1. 标题要吸睛，善用 '‼️', '✨', '🔥', '💯' 等符号，控制在20字以内
            2. 正文多用 Emoji，语气活泼（如'绝绝子', '暴风吸入', '按头安利', 'yyds', '一整个爱住'）
            3. 如果用户给了标签，必须在文中自然地体现这些标签的含义
            4. 如果没有标签，请根据图片内容通过想象力补充口感、氛围描述
            5. 分段清晰，正文控制在150-250字
            6. 结尾给出推荐指数（⭐⭐⭐⭐⭐ 或更少）

            【输出格式要求】
            请严格按以下格式输出，用 --- 分隔标题和正文：

            标题内容
            ---
            正文内容
            """;

    /**
     * 流式生成探店笔记
     *
     * @param request 生成请求
     * @param emitter SSE 发射器
     */
    @Override
    public void generateNoteStream(NoteGenerateRequest request, SseEmitter emitter) {
        // 1. 参数校验
        if (CollectionUtils.isEmpty(request.getImageUrls())) {
            sendError(emitter, "至少需要上传一张图片");
            return;
        }

        log.info("开始流式生成探店笔记，商家: {}, 图片数量: {}, 标签: {}",
                request.getShopName(),
                request.getImageUrls().size(),
                request.getTags());

        try {
            // 2. 构建系统消息
            SystemMessage systemMessage = SystemMessage.from(SYSTEM_PROMPT);

            // 3. 构建用户消息（多模态：文本 + 图片）
            UserMessage userMessage = buildUserMessage(request);

            // 4. 创建消息列表
            List<ChatMessage> messages = List.of(systemMessage, userMessage);

            // 5. 调用流式模型
            visionStreamingChatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    try {
                        // 发送每个 token 作为 SSE 事件
                        emitter.send(SseEmitter.event()
                                .name("token")
                                .data(Objects.requireNonNull(token, "token")));
                    } catch (IOException e) {
                        log.error("SSE 发送失败: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    try {
                        // 发送完成事件
                        emitter.send(SseEmitter.event()
                                .name("done")
                                .data("[DONE]"));
                        emitter.complete();
                        log.info("探店笔记流式生成完成");
                    } catch (IOException e) {
                        log.error("SSE 完成事件发送失败: {}", e.getMessage());
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("探店笔记流式生成失败", error);
                    sendError(emitter, "AI生成失败: " + error.getMessage());
                    emitter.completeWithError(error);
                }
            });

        } catch (Exception e) {
            log.error("探店笔记流式生成初始化失败", e);
            sendError(emitter, "初始化失败: " + e.getMessage());
            emitter.completeWithError(e);
        }
    }

    /**
     * 构建多模态用户消息
     * 包含文本说明和多张图片
     */
    private UserMessage buildUserMessage(NoteGenerateRequest request) {
        List<Content> contents = new ArrayList<>();

        // 1. 添加文本说明
        StringBuilder textPrompt = new StringBuilder();
        textPrompt.append("请帮我写一篇探店笔记。\n\n");

        // 商家名称
        if (StringUtils.hasText(request.getShopName())) {
            textPrompt.append("【商家名称】").append(request.getShopName()).append("\n");
        }

        // 用户标签
        if (!CollectionUtils.isEmpty(request.getTags())) {
            textPrompt.append("【我的感受标签】").append(String.join("、", request.getTags())).append("\n");
            textPrompt.append("请在笔记中自然融入这些标签所表达的感受。\n\n");
        } else {
            textPrompt.append("【我的感受标签】无\n");
            textPrompt.append("我没有提供标签，请完全基于图片内容发挥想象，描述食物的色泽、口感、环境氛围等。\n\n");
        }

        textPrompt.append("以下是我拍的照片，请仔细观察后生成笔记：");

        contents.add(TextContent.from(textPrompt.toString()));

        // 2. 添加所有图片
        for (String imageUrl : request.getImageUrls()) {
            log.debug("添加图片到消息: {}", imageUrl);
            contents.add(ImageContent.from(imageUrl));
        }

        return UserMessage.from(contents);
    }

    /**
     * 发送错误事件
     */
    private void sendError(SseEmitter emitter, String errorMessage) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(Objects.requireNonNull(errorMessage, "errorMessage")));
        } catch (IOException e) {
            log.error("SSE 错误事件发送失败: {}", e.getMessage());
        }
    }
}
