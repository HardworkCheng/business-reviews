package com.businessreviews.model.vo.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 生成回复响应 VO
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
