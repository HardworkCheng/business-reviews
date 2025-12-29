package com.businessreviews.model.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI智能生成探店笔记请求DTO
 * 
 * 包含三个主要输入：
 * - shopName: 商家名称（用于在笔记中提及店铺）
 * - imageUrls: 用户上传的图片URL列表（已上传至OSS的公网URL）
 * - tags: 用户选填的标签列表（如"好吃"、"量大"、"环境好"等）
 * 
 * @author businessreviews
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteGenerateRequest {

    /**
     * 商家名称
     * 用于在生成的笔记中提及店铺名称
     */
    private String shopName;

    /**
     * 图片URL列表
     * 必须是已上传到阿里云OSS的公网可访问URL
     * 至少需要1张图片，最多支持9张
     */
    private List<String> imageUrls;

    /**
     * 用户标签列表
     * 可选字段，用户可以选择或输入的标签（如"好吃"、"量大"、"服务好"）
     * 如果为空，AI将完全基于图片内容生成笔记
     */
    private List<String> tags;
}
