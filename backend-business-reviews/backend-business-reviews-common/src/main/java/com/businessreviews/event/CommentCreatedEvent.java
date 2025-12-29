package com.businessreviews.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 评论创建事件
 * 用于触发异步内容审核
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
