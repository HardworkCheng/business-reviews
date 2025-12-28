package com.businessreviews.model.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商家周报数据传输对象
 * 用于接收 LLM 分析后的结构化输出
 * 
 * @author businessreviews
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportDTO {

    /**
     * 情感评分（0-10分）
     * 0表示非常负面，10表示非常正面
     */
    private Integer sentimentScore;

    /**
     * 总体摘要（50字以内）
     * 概括性描述本周评论的整体情况
     */
    private String summary;

    /**
     * 优点列表（最多3个）
     * 从评论中提取的商家优势
     */
    private List<String> pros;

    /**
     * 缺点列表（最多3个）
     * 从评论中提取的待改进项
     */
    private List<String> cons;

    /**
     * 给商家的建议
     * 基于评论分析给出的改进建议
     */
    private String advice;

    /**
     * 分析的评论数量
     */
    private Integer reviewCount;

    /**
     * 报告生成时间
     */
    private String generatedAt;

    /**
     * 统计期间（如：2024-12-21 ~ 2024-12-28）
     */
    private String period;
}
