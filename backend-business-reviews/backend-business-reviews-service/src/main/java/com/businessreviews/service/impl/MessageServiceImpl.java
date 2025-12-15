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
import com.businessreviews.mapper.NoteMapper;
import com.businessreviews.mapper.NotificationMapper;
import com.businessreviews.mapper.SystemNoticeMapper;
import com.businessreviews.mapper.UserMapper;
import com.businessreviews.service.MessageService;
import com.businessreviews.entity.Note;
import com.businessreviews.entity.SystemNotice;
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
    private final SystemNoticeMapper systemNoticeMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final com.businessreviews.handler.MessageWebSocketHandler webSocketHandler;

    @Override
    public PageResult<ConversationResponse> getConversations(Long userId, Integer pageNum, Integer pageSize) {
        log.info("获取会话列表: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        
        // 获取用户的所有会话（分组查询最新消息）
        List<Map<String, Object>> conversations = messageMapper.selectConversations(userId, (pageNum - 1) * pageSize, pageSize);
        Long total = messageMapper.countConversations(userId);
        
        log.info("查询到会话数量: {}, 总数: {}", conversations.size(), total);
        
        List<ConversationResponse> list = conversations.stream()
                .map(conv -> {
                    log.debug("处理会话数据: {}", conv);
                    ConversationResponse response = new ConversationResponse();
                    
                    // 安全获取 other_user_id
                    Object otherUserIdObj = conv.get("other_user_id");
                    Long otherUserId = null;
                    if (otherUserIdObj instanceof Long) {
                        otherUserId = (Long) otherUserIdObj;
                    } else if (otherUserIdObj instanceof Number) {
                        otherUserId = ((Number) otherUserIdObj).longValue();
                    }
                    
                    if (otherUserId != null) {
                        User otherUser = userMapper.selectById(otherUserId);
                        if (otherUser != null) {
                            response.setUserId(otherUser.getId().toString());
                            response.setUsername(otherUser.getUsername());
                            response.setAvatar(otherUser.getAvatar());
                        } else {
                            response.setUserId(otherUserId.toString());
                            response.setUsername("未知用户");
                        }
                    }
                    
                    // 安全获取 last_message
                    Object lastMessageObj = conv.get("last_message");
                    response.setLastMessage(lastMessageObj != null ? lastMessageObj.toString() : "");
                    
                    // 安全获取 last_time
                    Object lastTimeObj = conv.get("last_time");
                    response.setLastTime(lastTimeObj != null ? lastTimeObj.toString() : "");
                    
                    // 安全获取 unread_count
                    Object unreadCountObj = conv.get("unread_count");
                    int unreadCount = 0;
                    if (unreadCountObj instanceof Number) {
                        unreadCount = ((Number) unreadCountObj).intValue();
                    }
                    response.setUnreadCount(unreadCount);
                    
                    return response;
                })
                .collect(Collectors.toList());
        
        log.info("返回会话列表: {}", list.size());
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
        
        MessageResponse response = convertToMessageResponse(message);
        
        // 通过WebSocket实时推送消息给接收者
        try {
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "private_message");
            
            // 构建消息数据，确保包含所有必要字段
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("id", response.getId());
            messageData.put("senderId", response.getSenderId());
            messageData.put("receiverId", response.getReceiverId());
            messageData.put("content", response.getContent());
            messageData.put("createdAt", response.getCreatedAt());
            messageData.put("senderName", response.getSenderName());
            messageData.put("senderAvatar", response.getSenderAvatar());
            
            wsMessage.put("data", messageData);
            
            log.info("准备推送WebSocket消息: targetUserId={}, message={}", targetUserId, wsMessage);
            webSocketHandler.sendMessageToUser(targetUserId, wsMessage);
            log.info("WebSocket消息已推送: targetUserId={}", targetUserId);
        } catch (Exception e) {
            log.error("WebSocket消息推送失败", e);
            // 不影响消息发送，只是实时推送失败
        }
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long targetUserId) {
        messageMapper.markAsRead(userId, targetUserId);
    }

    @Override
    public PageResult<NotificationResponse> getNotifications(Long userId, Integer type, Integer pageNum, Integer pageSize) {
        // 使用 system_notices 表获取通知（包含发送者信息）
        Page<SystemNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SystemNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotice::getUserId, userId);
        
        if (type != null) {
            wrapper.eq(SystemNotice::getNoticeType, type);
        }
        
        wrapper.orderByDesc(SystemNotice::getCreatedAt);
        
        Page<SystemNotice> noticePage = systemNoticeMapper.selectPage(page, wrapper);
        
        List<NotificationResponse> list = noticePage.getRecords().stream()
                .map(this::convertSystemNoticeToResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(list, noticePage.getTotal(), pageNum, pageSize);
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemNotice(Long userId, Long fromUserId, Integer type, Long targetId, String content, String imageUrl) {
        SystemNotice notice = new SystemNotice();
        notice.setUserId(userId);
        notice.setFromUserId(fromUserId);
        notice.setNoticeType(type);
        notice.setTargetId(targetId);
        notice.setContent(content);
        notice.setImageUrl(imageUrl);
        notice.setIsRead(0);
        
        systemNoticeMapper.insert(notice);
        log.info("发送系统通知: userId={}, fromUserId={}, type={}, targetId={}", userId, fromUserId, type, targetId);
    }

    private int countUnreadNotifications(Long userId, Integer type) {
        LambdaQueryWrapper<SystemNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotice::getUserId, userId)
               .eq(SystemNotice::getNoticeType, type)
               .eq(SystemNotice::getIsRead, 0);
        return systemNoticeMapper.selectCount(wrapper).intValue();
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
    
    private NotificationResponse convertSystemNoticeToResponse(SystemNotice notice) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notice.getId().toString());
        response.setType(notice.getNoticeType());
        response.setContent(notice.getContent());
        response.setRelatedId(notice.getTargetId() != null ? notice.getTargetId().toString() : null);
        response.setIsRead(notice.getIsRead() != null && notice.getIsRead() == 1);
        response.setCreatedAt(notice.getCreatedAt() != null ? notice.getCreatedAt().toString() : "");
        response.setTimeAgo(notice.getCreatedAt() != null ? TimeUtil.formatRelativeTime(notice.getCreatedAt()) : "");
        
        // 获取发送者信息
        if (notice.getFromUserId() != null) {
            User fromUser = userMapper.selectById(notice.getFromUserId());
            if (fromUser != null) {
                response.setFromUserId(fromUser.getId());
                response.setFromUsername(fromUser.getUsername());
                response.setFromAvatar(fromUser.getAvatar());
            }
        }
        
        // 获取笔记信息（如果是点赞笔记或评论笔记类型）
        if (notice.getTargetId() != null && (notice.getNoticeType() == 1 || notice.getNoticeType() == 2)) {
            Note note = noteMapper.selectById(notice.getTargetId());
            if (note != null) {
                response.setNoteId(note.getId());
                response.setNoteTitle(note.getTitle());
                response.setNoteImage(note.getCoverImage());
            }
        }
        
        // 设置标题
        switch (notice.getNoticeType()) {
            case 1:
                response.setTitle("点赞通知");
                break;
            case 2:
                response.setTitle("评论通知");
                break;
            case 3:
                response.setTitle("关注通知");
                break;
            case 4:
                response.setTitle("点赞评论");
                break;
            default:
                response.setTitle("系统通知");
        }
        
        return response;
    }
}
