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
 * <p>
 * 提供移动端用户的消息和通知API：
 * - GET /messages/conversations - 获取会话列表
 * - GET /messages/chat/{targetUserId} - 获取聊天记录
 * - POST /messages/send - 发送消息
 * - POST /messages/read/{targetUserId} - 标记消息已读
 * - GET /messages/notifications - 获取系统通知列表
 * - POST /messages/notifications/{id}/read - 标记通知已读
 * - POST /messages/notifications/read-all - 标记所有通知已读
 * - GET /messages/unread-count - 获取未读消息数量
 * </p>
 *
 * @author businessreviews
 * @see com.businessreviews.service.app.MessageService
 */
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取会话列表
     */
    /**
     * 获取会话列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 会话列表PageResult
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
    /**
     * 获取聊天记录
     *
     * @param targetUserId 目标用户ID
     * @param pageNum      页码
     * @param pageSize     每页数量
     * @return 聊天记录PageResult
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
    /**
     * 发送消息
     *
     * @param request 发送请求（接收人、内容、类型）
     * @return 发送成功的消息VO
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
    /**
     * 标记消息已读
     *
     * @param targetUserId 目标用户ID（将此用户发来的消息标记为已读）
     * @return 成功结果
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
    /**
     * 获取系统通知列表
     *
     * @param type     通知类型
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 通知列表PageResult
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
    /**
     * 标记通知已读
     *
     * @param id 通知ID
     * @return 成功结果
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
    /**
     * 标记所有通知已读
     *
     * @param type 通知类型 (可选)
     * @return 成功结果
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
    /**
     * 获取未读消息数量 (包含私信和通知)
     *
     * @return 统计结果
     */
    @GetMapping("/unread-count")
    public Result<Object> getUnreadCount() {
        Long userId = UserContext.requireUserId();
        Object result = messageService.getUnreadCount(userId);
        return Result.success(result);
    }

    /**
     * 分享笔记给用户
     */
    /**
     * 分享笔记给用户
     *
     * @param request 分享请求
     * @return 成功结果
     */
    @PostMapping("/share-note")
    public Result<?> shareNote(@RequestBody @Valid com.businessreviews.model.dto.app.ShareNoteDTO request) {
        Long userId = UserContext.requireUserId();
        messageService.shareNoteToUsers(userId, request.getNoteId(), request.getUserIds());
        return Result.success("分享成功");
    }

    /**
     * 分享店铺给用户
     */
    /**
     * 分享店铺给用户
     *
     * @param request 分享请求
     * @return 成功结果
     */
    @PostMapping("/share-shop")
    public Result<?> shareShop(@RequestBody @Valid com.businessreviews.model.dto.app.ShareShopDTO request) {
        Long userId = UserContext.requireUserId();
        messageService.shareShopToUsers(userId, request.getShopId(), request.getUserIds());
        return Result.success("分享成功");
    }
}
