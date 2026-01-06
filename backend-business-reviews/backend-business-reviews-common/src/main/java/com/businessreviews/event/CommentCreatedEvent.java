package com.businessreviews.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 评论创建事件
 * <p>
 * 当用户发布新评论后发布此事件，用于解耦触发：
 * 1. 内容安全审核（文本/图片）
 * 2. 消息通知（通知被评论人）
 * 3. 统计数据更新
 * </p>
 * 
 * @author businessreviews
 */
@Getter
public class CommentCreatedEvent extends ApplicationEvent {

    /**
     * 评论ID
     */
    private final Long commentId;

    /**
     * 评论内容
     */
    private final String content;

    /**
     * 评论用户ID
     */
    private final Long userId;

    /**
     * 关联的笔记ID
     */
    private final Long noteId;

    public CommentCreatedEvent(Object source, Long commentId, String content, Long userId, Long noteId) {
        super(source);
        this.commentId = commentId;
        this.content = content;
        this.userId = userId;
        this.noteId = noteId;
    }
}
