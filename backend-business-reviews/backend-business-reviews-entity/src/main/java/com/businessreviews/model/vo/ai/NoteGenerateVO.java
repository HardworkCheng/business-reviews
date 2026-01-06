package com.businessreviews.model.vo.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI智能生成探店笔记响应对象
 * <p>
 * 包含生成的笔记标题和正文内容，符合平台内容风格
 * </p>
 * 
 * @author businessreviews
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteGenerateVO {

    /**
     * 生成的笔记标题
     * 小红书风格，使用吸睛符号如 ‼️ ✨ 等
     */
    private String title;

    /**
     * 生成的笔记正文
     * 小红书风格，Emoji丰富、语气年轻活泼、分段清晰、约200字
     */
    private String content;
}
