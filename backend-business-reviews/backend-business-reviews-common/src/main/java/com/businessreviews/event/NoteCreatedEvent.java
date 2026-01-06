package com.businessreviews.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 笔记创建事件
 * <p>
 * 当用户发布新笔记后发布此事件，核心用途：
 * 1. 触发AI内容审核（异步）
 * 2. 触发粉丝推送
 * 3. 触发搜索索引更新
 * </p>
 * 
 * 事件驱动设计优点：
 * 1. 解耦：审核逻辑与发布逻辑分离，便于后续扩展
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
