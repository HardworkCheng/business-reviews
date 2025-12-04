package com.businessreviews.controller;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.dto.request.SendMessageRequest;
import com.businessreviews.dto.response.ConversationResponse;
import com.businessreviews.dto.response.MessageResponse;
import com.businessreviews.dto.response.NotificationResponse;
import com.businessreviews.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<PageResult<ConversationResponse>> getConversations(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<ConversationResponse> result = messageService.getConversations(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/chat/{targetUserId}")
    public Result<PageResult<MessageResponse>> getChatHistory(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<MessageResponse> result = messageService.getChatHistory(userId, targetUserId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<MessageResponse> sendMessage(@RequestBody @Valid SendMessageRequest request) {
        Long userId = UserContext.requireUserId();
        MessageResponse response = messageService.sendMessage(userId, 
                Long.parseLong(request.getTargetUserId()), 
                request.getContent(), 
                request.getType());
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
    public Result<PageResult<NotificationResponse>> getNotifications(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<NotificationResponse> result = messageService.getNotifications(userId, type, pageNum, pageSize);
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
