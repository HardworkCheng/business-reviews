package com.businessreviews.web.app;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.model.dto.app.SendMessageDTO;
import com.businessreviews.model.vo.ConversationVO;
import com.businessreviews.model.vo.MessageVO;
import com.businessreviews.model.vo.NotificationVO;
import com.businessreviews.service.app.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 移动端消息控制器 (UniApp)
 * 
 * 提供移动端用户的消息和通知API：
 * - GET /messages/conversations - 获取会话列表
 * - GET /messages/chat/{targetUserId} - 获取聊天记录
 * - POST /messages/send - 发送消息
 * - POST /messages/read/{targetUserId} - 标记消息已读
 * - GET /messages/notifications - 获取系统通知列表
 * - POST /messages/notifications/{id}/read - 标记通知已读
 * - POST /messages/notifications/read-all - 标记所有通知已读
 * - GET /messages/unread-count - 获取未读消息数量
 * 
 * @see com.businessreviews.service.MessageService
 */
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<PageResult<ConversationVO>> getConversations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<ConversationVO> result = messageService.getConversations(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/chat/{targetUserId}")
    public Result<PageResult<MessageVO>> getChatHistory(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<MessageVO> result = messageService.getChatHistory(userId, targetUserId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<MessageVO> sendMessage(@RequestBody @Valid SendMessageDTO request) {
        Long userId = UserContext.requireUserId();
        MessageVO response = messageService.sendMessage(userId, 
                request.getReceiverId(), 
                request.getContent(), 
                request.getMessageType());
        return Result.success("发送成功", response);
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/read/{targetUserId}")
    public Result<?> markAsRead(@PathVariable Long targetUserId) {
        Long userId = UserContext.requireUserId();
        messageService.markAsRead(userId, targetUserId);
        return Result.success("标记成功");
    }

    /**
     * 获取系统通知列表
     */
    @GetMapping("/notifications")
    public Result<PageResult<NotificationVO>> getNotifications(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<NotificationVO> result = messageService.getNotifications(userId, type, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 标记通知已读
     */
    @PostMapping("/notifications/{id}/read")
    public Result<?> markNotificationAsRead(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        messageService.markNotificationAsRead(userId, id);
        return Result.success("标记成功");
    }

    /**
     * 标记所有通知已读
     */
    @PostMapping("/notifications/read-all")
    public Result<?> markAllNotificationsAsRead(@RequestParam(required = false) Integer type) {
        Long userId = UserContext.requireUserId();
        messageService.markAllNotificationsAsRead(userId, type);
        return Result.success("标记成功");
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Object> getUnreadCount() {
        Long userId = UserContext.requireUserId();
        Object result = messageService.getUnreadCount(userId);
        return Result.success(result);
    }
}
