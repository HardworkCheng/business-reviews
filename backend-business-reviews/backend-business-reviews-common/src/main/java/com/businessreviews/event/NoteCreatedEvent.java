package com.businessreviews.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 笔记创建事件
 * 用于触发异步内容审核
 * 
 * 事件驱动设计优点：
 * 1. 解耦：审核逻辑与发布逻辑分离，便于后续扩展（如增加敏感词检测、关键词统计等）
 * 2. 异步：主线程快速返回，用户体验好
 * 3. 可扩展：新增监听器无需修改发布代码
 * 
 * @author businessreviews
 */
@Getter
public class NoteCreatedEvent extends ApplicationEvent {

    /**
     * 笔记ID
     */
    private final Long noteId;

    /**
     * 笔记标题
     */
    private final String title;

    /**
     * 笔记内容
     */
    private final String content;

    /**
     * 发布用户ID
     */
    private final Long userId;

    public NoteCreatedEvent(Object source, Long noteId, String title, String content, Long userId) {
        super(source);
        this.noteId = noteId;
        this.title = title;
        this.content = content;
        this.userId = userId;
    }
}
