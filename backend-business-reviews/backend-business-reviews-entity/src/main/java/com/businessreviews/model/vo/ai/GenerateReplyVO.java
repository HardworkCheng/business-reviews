package com.businessreviews.model.vo.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 生成回复响应对象
 * <p>
 * 用于封装AI生成的回复内容及元数据
 * </p>
 * 
 * @author businessreviews
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReplyVO {

    /**
     * AI 生成的回复内容
     */
    private String reply;

    /**
     * 生成时间
     */
    private String generatedAt;
}
