package com.businessreviews.listener;

import com.businessreviews.event.CommentCreatedEvent;
import com.businessreviews.event.NoteCreatedEvent;
import com.businessreviews.model.dto.ai.AuditResult;
import com.businessreviews.service.ai.ContentSecurityService;
import com.businessreviews.service.app.MessageService;
import com.businessreviews.mapper.NoteMapper;
import com.businessreviews.mapper.CommentMapper;
import com.businessreviews.model.dataobject.NoteDO;
import com.businessreviews.model.dataobject.CommentDO;
import com.businessreviews.enums.NoteStatus;
import com.businessreviews.enums.CommentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 内容审核事件监听器
 * 异步处理笔记和评论的AI审核
 * 
 * 设计说明：
 * 1. 使用 @Async 确保审核在独立线程中执行，不阻塞主线程
 * 2. 使用 @EventListener 监听内容创建事件，实现解耦
 * 3. 事务分离：发布事务已提交，审核是新的事务
 * 4. 审核不通过时，自动发送通知给用户告知屏蔽原因
 * 
 * 状态说明：
 * - 笔记状态：1=正常/已发布, 2=隐藏/已拒绝, 3=审核中
 * - 评论状态：1=正常, 2=隐藏/已拒绝
 * 
 * @author businessreviews
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final ContentSecurityService contentSecurityService;
    private final MessageService messageService;
    private final NoteMapper noteMapper;
    private final CommentMapper commentMapper;

    /**
     * 异步审核笔记内容
     * 
     * 流程：
     * 1. 调用AI审核服务
     * 2. 根据审核结果更新笔记状态
     * 3. 如果审核不通过，发送通知给用户
     * 4. 记录审核日志
     */
    @Async("asyncExecutor")
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleNoteCreated(NoteCreatedEvent event) {
        Long noteId = event.getNoteId();
        Long userId = event.getUserId();
        log.info("开始异步审核笔记，ID: {}, 标题: {}", noteId, event.getTitle());

        try {
            // 1. 调用AI审核
            AuditResult result = contentSecurityService.auditNote(event.getTitle(), event.getContent());

            // 2. 根据审核结果更新数据库
            NoteDO note = noteMapper.selectById(noteId);
            if (note == null) {
                log.warn("笔记不存在，跳过审核更新，ID: {}", noteId);
                return;
            }

            if (result.isSafe()) {
                // 审核通过：状态更新为正常/已发布
                note.setStatus(NoteStatus.NORMAL.getCode());
                noteMapper.updateById(note);
                log.info("笔记 [{}] 审核通过，已发布", noteId);
            } else {
                // 审核不通过：状态更新为隐藏/已拒绝
                note.setStatus(NoteStatus.HIDDEN.getCode());
                noteMapper.updateById(note);
                log.warn("笔记 [{}] 审核不通过，类型: {}, 原因: {}, 建议: {}",
                        noteId, result.getType(), result.getReason(), result.getSuggestion());

                // 3. 发送通知给用户，告知屏蔽原因
                try {
                    messageService.sendAuditNotification(
                            userId,
                            "笔记",
                            event.getTitle(),
                            result.getType().getName() + " - " + result.getReason(),
                            result.getSuggestion());
                    log.info("已向用户 [{}] 发送笔记屏蔽通知", userId);
                } catch (Exception e) {
                    log.error("发送笔记屏蔽通知失败: userId={}, noteId={}, error={}",
                            userId, noteId, e.getMessage());
                }
            }

            log.info("笔记 [{}] 审核完成，最终状态: {}", noteId, note.getStatus());

        } catch (Exception e) {
            log.error("笔记审核异常，ID: {}, 错误: {}", noteId, e.getMessage(), e);
            // 审核异常时不改变状态，保持审核中状态，等待人工处理
        }
    }

    /**
     * 异步审核评论内容
     * 
     * 流程：
     * 1. 调用AI审核服务
     * 2. 根据审核结果更新评论状态
     * 3. 如果审核不通过，发送通知给用户
     */
    @Async("asyncExecutor")
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleCommentCreated(CommentCreatedEvent event) {
        Long commentId = event.getCommentId();
        Long userId = event.getUserId();
        log.info("开始异步审核评论，ID: {}", commentId);

        try {
            // 1. 调用AI审核
            AuditResult result = contentSecurityService.auditComment(event.getContent());

            // 2. 根据审核结果更新数据库
            CommentDO comment = commentMapper.selectById(commentId);
            if (comment == null) {
                log.warn("评论不存在，跳过审核更新，ID: {}", commentId);
                return;
            }

            if (result.isSafe()) {
                // 评论默认就是状态1（正常），无需更新
                log.info("评论 [{}] 审核通过", commentId);
            } else {
                // 审核不通过：状态更新为隐藏
                comment.setStatus(CommentStatus.HIDDEN.getCode());
                commentMapper.updateById(comment);
                log.warn("评论 [{}] 审核不通过，类型: {}, 原因: {}",
                        commentId, result.getType(), result.getReason());

                // 3. 发送通知给用户，告知屏蔽原因
                try {
                    // 评论内容作为标题（截取前30字）
                    String commentPreview = event.getContent();
                    if (commentPreview != null && commentPreview.length() > 30) {
                        commentPreview = commentPreview.substring(0, 30) + "...";
                    }

                    messageService.sendAuditNotification(
                            userId,
                            "评论",
                            commentPreview,
                            result.getType().getName() + " - " + result.getReason(),
                            result.getSuggestion());
                    log.info("已向用户 [{}] 发送评论屏蔽通知", userId);
                } catch (Exception e) {
                    log.error("发送评论屏蔽通知失败: userId={}, commentId={}, error={}",
                            userId, commentId, e.getMessage());
                }
            }

            log.info("评论 [{}] 审核完成，最终状态: {}", commentId, comment.getStatus());

        } catch (Exception e) {
            log.error("评论审核异常，ID: {}, 错误: {}", commentId, e.getMessage(), e);
        }
    }
}
