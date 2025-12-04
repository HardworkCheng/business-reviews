package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.response.ConversationResponse;
import com.businessreviews.dto.response.MessageResponse;
import com.businessreviews.dto.response.NotificationResponse;
import com.businessreviews.dto.response.UnreadCountResponse;
import com.businessreviews.entity.Message;
import com.businessreviews.entity.Notification;
import com.businessreviews.entity.User;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.MessageMapper;
import com.businessreviews.mapper.NotificationMapper;
import com.businessreviews.mapper.UserMapper;
import com.businessreviews.service.MessageService;
import com.businessreviews.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final MessageMapper messageMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<ConversationResponse> getConversations(Long userId, Integer pageNum, Integer pageSize) {
        // 获取用户的所有会话（分组查询最新消息）
        List<Map<String, Object>> conversations = messageMapper.selectConversations(userId, (pageNum - 1) * pageSize, pageSize);
        Long total = messageMapper.countConversations(userId);
        
        List<ConversationResponse> list = conversations.stream()
                .map(conv -> {
                    ConversationResponse response = new ConversationResponse();
                    Long otherUserId = (Long) conv.get("other_user_id");
                    User otherUser = userMapper.selectById(otherUserId);
                    
                    if (otherUser != null) {
                        response.setUserId(otherUser.getId().toString());
                        response.setUsername(otherUser.getUsername());
                        response.setAvatar(otherUser.getAvatar());
                    }
                    
                    response.setLastMessage((String) conv.get("last_message"));
                    response.setLastTime(conv.get("last_time").toString());
                    response.setUnreadCount(((Number) conv.get("unread_count")).intValue());
                    
                    return response;
                })
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, pageNum, pageSize);
    }

    @Override
    public PageResult<MessageResponse> getChatHistory(Long userId, Long targetUserId, Integer pageNum, Integer pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        
        // 查询两个用户之间的消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getSenderId, userId).eq(Message::getReceiverId, targetUserId))
               .or(w -> w.eq(Message::getSenderId, targetUserId).eq(Message::getReceiverId, userId))
               .orderByDesc(Message::getCreatedAt);
        
        Page<Message> messagePage = messageMapper.selectPage(page, wrapper);
        
        List<MessageResponse> list = messagePage.getRecords().stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(list, messagePage.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageResponse sendMessage(Long userId, Long targetUserId, String content, Integer type) {
        // 检查目标用户是否存在
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        
        Message message = new Message();
        message.setSenderId(userId);
        message.setReceiverId(targetUserId);
        message.setContent(content);
        message.setType(type != null ? type : 1);
        message.setIsRead(false);
        
        messageMapper.insert(message);
        
        return convertToMessageResponse(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long targetUserId) {
        messageMapper.markAsRead(userId, targetUserId);
    }

    @Override
    public PageResult<NotificationResponse> getNotifications(Long userId, Integer type, Integer pageNum, Integer pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        
        if (type != null) {
            wrapper.eq(Notification::getType, type);
        }
        
        wrapper.orderByDesc(Notification::getCreatedAt);
        
        Page<Notification> notificationPage = notificationMapper.selectPage(page, wrapper);
        
        List<NotificationResponse> list = notificationPage.getRecords().stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(list, notificationPage.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markNotificationAsRead(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new BusinessException(40402, "通知不存在");
        }
        
        notification.setIsRead(true);
        notificationMapper.updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllNotificationsAsRead(Long userId, Integer type) {
        notificationMapper.markAllAsRead(userId, type);
    }

    @Override
    public Object getUnreadCount(Long userId) {
        UnreadCountResponse response = new UnreadCountResponse();
        
        // 统计未读私信数
        LambdaQueryWrapper<Message> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(Message::getReceiverId, userId)
                      .eq(Message::getIsRead, false);
        response.setMessageCount(messageMapper.selectCount(messageWrapper).intValue());
        
        // 统计未读点赞通知数
        response.setLikeCount(countUnreadNotifications(userId, 1));
        
        // 统计未读评论通知数
        response.setCommentCount(countUnreadNotifications(userId, 2));
        
        // 统计未读关注通知数
        response.setFollowCount(countUnreadNotifications(userId, 3));
        
        // 统计未读系统通知数
        response.setSystemCount(countUnreadNotifications(userId, 4));
        
        // 总数
        response.setTotal(response.getMessageCount() + response.getLikeCount() + 
                         response.getCommentCount() + response.getFollowCount() + 
                         response.getSystemCount());
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, String title, String content, Integer type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);
        
        notificationMapper.insert(notification);
    }

    private int countUnreadNotifications(Long userId, Integer type) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getType, type)
               .eq(Notification::getIsRead, false);
        return notificationMapper.selectCount(wrapper).intValue();
    }

    private MessageResponse convertToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId().toString());
        response.setSenderId(message.getSenderId().toString());
        response.setReceiverId(message.getReceiverId().toString());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setIsRead(message.getIsRead());
        response.setCreatedAt(message.getCreatedAt().toString());
        response.setTimeAgo(TimeUtil.formatRelativeTime(message.getCreatedAt()));
        
        // 查询发送者信息
        User sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            response.setSenderName(sender.getUsername());
            response.setSenderAvatar(sender.getAvatar());
        }
        
        return response;
    }

    private NotificationResponse convertToNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId().toString());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setType(notification.getType());
        response.setRelatedId(notification.getRelatedId() != null ? notification.getRelatedId().toString() : null);
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt().toString());
        response.setTimeAgo(TimeUtil.formatRelativeTime(notification.getCreatedAt()));
        
        return response;
    }
}
