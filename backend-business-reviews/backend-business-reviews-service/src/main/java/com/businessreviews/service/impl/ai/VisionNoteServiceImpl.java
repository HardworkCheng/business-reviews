package com.businessreviews.service.impl.ai;

import com.businessreviews.model.dto.ai.NoteGenerateRequest;
import com.businessreviews.model.vo.ai.NoteGenerateVO;
import com.businessreviews.service.ai.VisionNoteService;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能探店笔记生成服务实现
 * 
 * 使用LangChain4j的ChatLanguageModel手动构建多模态消息
 * 支持识别多张图片并结合用户标签生成小红书风格笔记
 * 
 * 注意：此服务使用通义千问 Qwen-VL 视觉模型，支持图片识别
 * 
 * @author businessreviews
 */
@Slf4j
@Service
public class VisionNoteServiceImpl implements VisionNoteService {

    private final ChatLanguageModel visionChatModel;

    /**
     * 构造函数注入视觉模型
     * 使用 @Qualifier 指定注入 visionChatModel（通义千问 Qwen-VL）
     */
    public VisionNoteServiceImpl(@Qualifier("visionChatModel") ChatLanguageModel visionChatModel) {
        this.visionChatModel = visionChatModel;
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
     * 生成探店笔记
     * <p>
     * 调用多模态大模型（Vision Model），基于图片和标签生成图文并茂的探店笔记。
     * 自动提取标题和正文。
     * </p>
     *
     * @param request 生成请求（图片URL列表、标签等）
     * @return 生成结果VO（title, content）
     */
    @Override
    public NoteGenerateVO generateNote(NoteGenerateRequest request) {
        // 1. 参数校验
        if (CollectionUtils.isEmpty(request.getImageUrls())) {
            throw new IllegalArgumentException("至少需要上传一张图片");
        }

        log.info("开始生成探店笔记，商家: {}, 图片数量: {}, 标签: {}",
                request.getShopName(),
                request.getImageUrls().size(),
                request.getTags());

        try {
            // 2. 构建系统消息
            SystemMessage systemMessage = SystemMessage.from(SYSTEM_PROMPT);

            // 3. 构建用户消息（多模态：文本 + 图片）
            UserMessage userMessage = buildUserMessage(request);

            // 4. 调用AI模型
            Response<AiMessage> response = visionChatModel.generate(systemMessage, userMessage);
            String generatedText = response.content().text();

            log.info("AI生成完成，原始响应长度: {} 字符", generatedText.length());

            // 5. 解析标题和正文
            return parseGeneratedContent(generatedText);

        } catch (Exception e) {
            log.error("AI生成探店笔记失败", e);
            // 返回默认内容作为兜底
            return generateDefaultNote(request);
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
     * 解析AI生成的内容，提取标题和正文
     */
    private NoteGenerateVO parseGeneratedContent(String generatedText) {
        String title;
        String content;

        // 尝试用 "---" 分割标题和正文
        if (generatedText.contains("---")) {
            String[] parts = generatedText.split("---", 2);
            title = parts[0].trim();
            content = parts.length > 1 ? parts[1].trim() : "";
        } else if (generatedText.contains("\n\n")) {
            // 备用方案：用双换行分割
            String[] parts = generatedText.split("\n\n", 2);
            title = parts[0].trim();
            content = parts.length > 1 ? parts[1].trim() : "";
        } else {
            // 兜底：取第一行作为标题
            String[] lines = generatedText.split("\n", 2);
            title = lines[0].trim();
            content = lines.length > 1 ? lines[1].trim() : "";
        }

        // 清理标题（移除可能的"标题："前缀）
        title = title.replaceFirst("^(标题[:：]?\\s*)", "");

        // 清理正文（移除可能的"正文："前缀）
        content = content.replaceFirst("^(正文[:：]?\\s*)", "");

        return NoteGenerateVO.builder()
                .title(title)
                .content(content)
                .build();
    }

    /**
     * 生成默认笔记（AI调用失败时的兜底方案）
     */
    private NoteGenerateVO generateDefaultNote(NoteGenerateRequest request) {
        String shopName = StringUtils.hasText(request.getShopName())
                ? request.getShopName()
                : "这家店";

        String title = "✨ 发现宝藏小店！" + shopName + " 必吃 ‼️";

        StringBuilder content = new StringBuilder();
        content.append("今天来探店啦！🎉\n\n");
        content.append("一进门就被环境惊艳到了～\n");
        content.append("拍照超级出片📸\n\n");

        if (!CollectionUtils.isEmpty(request.getTags())) {
            content.append("我的感受就是：");
            content.append(String.join(" ", request.getTags()));
            content.append(" 👍\n\n");
        }

        content.append("下次还会再来的！\n");
        content.append("推荐指数：⭐⭐⭐⭐⭐");

        return NoteGenerateVO.builder()
                .title(title)
                .content(content.toString())
                .build();
    }
}
