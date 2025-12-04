package com.businessreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.response.ConversationResponse;
import com.businessreviews.dto.response.MessageResponse;
import com.businessreviews.dto.response.NotificationResponse;
import com.businessreviews.entity.Message;

public interface MessageService extends IService<Message> {
    
    /**
     * 获取会话列表
     */
    PageResult<ConversationResponse> getConversations(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取聊天记录
     */
    PageResult<MessageResponse> getChatHistory(Long userId, Long targetUserId, Integer pageNum, Integer pageSize);
    
    /**
     * 发送消息
     */
    MessageResponse sendMessage(Long userId, Long targetUserId, String content, Integer type);
    
    /**
     * 标记消息已读
     */
    void markAsRead(Long userId, Long targetUserId);
    
    /**
     * 获取系统通知列表
     */
    PageResult<NotificationResponse> getNotifications(Long userId, Integer type, Integer pageNum, Integer pageSize);
    
    /**
     * 标记通知已读
     */
    void markNotificationAsRead(Long userId, Long notificationId);
    
    /**
     * 标记所有通知已读
     */
    void markAllNotificationsAsRead(Long userId, Integer type);
    
    /**
     * 获取未读消息数量
     */
    Object getUnreadCount(Long userId);
    
    /**
     * 发送系统通知
     */
    void sendNotification(Long userId, String title, String content, Integer type, Long relatedId);
}
