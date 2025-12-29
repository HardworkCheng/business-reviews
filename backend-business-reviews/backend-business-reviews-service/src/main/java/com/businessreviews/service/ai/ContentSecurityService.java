package com.businessreviews.service.ai;

import com.businessreviews.model.dto.ai.AuditResult;

/**
 * 内容安全服务接口
 * 提供笔记/评论内容的AI审核功能
 * 
 * @author businessreviews
 */
public interface ContentSecurityService {

    /**
     * 审核文本内容
     * 
     * @param text 待审核的文本内容
     * @return AuditResult 审核结果
     */
    AuditResult auditContent(String text);

    /**
     * 审核笔记内容（标题+正文）
     * 
     * @param title   笔记标题
     * @param content 笔记内容
     * @return AuditResult 审核结果
     */
    AuditResult auditNote(String title, String content);

    /**
     * 审核评论内容
     * 
     * @param content 评论内容
     * @return AuditResult 审核结果
     */
    AuditResult auditComment(String content);
}
