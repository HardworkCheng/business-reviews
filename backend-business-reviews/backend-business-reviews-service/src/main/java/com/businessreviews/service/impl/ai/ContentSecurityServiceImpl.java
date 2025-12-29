package com.businessreviews.service.impl.ai;

import com.businessreviews.enums.ViolationType;
import com.businessreviews.model.dto.ai.AuditResult;
import com.businessreviews.service.ai.ContentAuditAgent;
import com.businessreviews.service.ai.ContentSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 内容安全服务实现类
 * 调用 ContentAuditAgent 进行AI内容审核
 * 
 * 使用示例：
 * 
 * <pre>
 * AuditResult result = contentSecurityService.auditContent("待审核的文本");
 * if (!result.isSafe()) {
 *     // 拦截发布，返回错误信息给前端
 *     throw new BusinessException("内容违规: " + result.getReason());
 * }
 * </pre>
 * 
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSecurityServiceImpl implements ContentSecurityService {

    private final ContentAuditAgent contentAuditAgent;

    @Override
    public AuditResult auditContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("审核内容为空，直接返回安全结果");
            return AuditResult.safe();
        }

        log.info("开始AI内容审核，内容长度: {} 字符", text.length());
        long startTime = System.currentTimeMillis();

        try {
            // 调用AI Agent进行审核
            AuditResult result = contentAuditAgent.auditContent(text);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("AI审核完成，耗时: {}ms，结果: isSafe={}, type={}, reason={}",
                    elapsed, result.isSafe(), result.getType(), result.getReason());

            return result;

        } catch (Exception e) {
            log.error("AI审核异常，降级处理为安全（允许发布，人工复查）: {}", e.getMessage(), e);
            // 审核服务异常时的降级策略：允许发布，后续人工复查
            // 注意：生产环境可根据业务需求调整为拒绝发布
            return AuditResult.builder()
                    .isSafe(true)
                    .type(ViolationType.SAFE)
                    .reason("审核服务暂时不可用")
                    .suggestion("内容已提交，系统将进行复查")
                    .build();
        }
    }

    @Override
    public AuditResult auditNote(String title, String content) {
        // 将标题和内容合并进行审核
        StringBuilder fullContent = new StringBuilder();
        if (title != null && !title.trim().isEmpty()) {
            fullContent.append("【标题】").append(title).append("\n");
        }
        if (content != null && !content.trim().isEmpty()) {
            fullContent.append("【正文】").append(content);
        }

        log.info("审核笔记内容，标题: {}", title);
        return auditContent(fullContent.toString());
    }

    @Override
    public AuditResult auditComment(String content) {
        log.info("审核评论内容，长度: {} 字符", content != null ? content.length() : 0);
        return auditContent(content);
    }
}
