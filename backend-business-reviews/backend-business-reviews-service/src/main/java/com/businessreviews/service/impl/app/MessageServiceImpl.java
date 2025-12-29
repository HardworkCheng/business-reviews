package com.businessreviews.service.impl.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.ConversationVO;
import com.businessreviews.model.vo.MessageVO;
import com.businessreviews.model.vo.NotificationVO;
import com.businessreviews.model.vo.UnreadCountVO;
import com.businessreviews.model.dataobject.MessageDO;
import com.businessreviews.model.dataobject.NotificationDO;
import com.businessreviews.model.dataobject.UserDO;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.MessageMapper;
import com.businessreviews.mapper.NoteMapper;
import com.businessreviews.mapper.NotificationMapper;
import com.businessreviews.mapper.SystemNoticeMapper;
import com.businessreviews.mapper.UserMapper;
import com.businessreviews.service.app.MessageService;
import com.businessreviews.model.dataobject.NoteDO;
import com.businessreviews.model.dataobject.SystemNoticeDO;
import com.businessreviews.util.TimeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, MessageDO> implements MessageService {

    private final MessageMapper messageMapper;
    private final NotificationMapper notificationMapper;
    private final SystemNoticeMapper systemNoticeMapper;
    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final com.businessreviews.mapper.ShopMapper shopMapper;
    private final com.businessreviews.handler.MessageWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<ConversationVO> getConversations(Long userId, Integer pageNum, Integer pageSize) {
        log.info("获取会话列表: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);

        // 获取用户的所有会话（分组查询最新消息）
        List<Map<String, Object>> conversations = messageMapper.selectConversations(userId, (pageNum - 1) * pageSize,
                pageSize);
        Long total = messageMapper.countConversations(userId);

        log.info("查询到会话数量: {}, 总数: {}", conversations.size(), total);

        List<ConversationVO> list = conversations.stream()
                .map(conv -> {
                    log.debug("处理会话数据: {}", conv);
                    ConversationVO response = new ConversationVO();

                    // 安全获取 other_user_id
                    Object otherUserIdObj = conv.get("other_user_id");
                    Long otherUserId = null;
                    if (otherUserIdObj instanceof Long) {
                        otherUserId = (Long) otherUserIdObj;
                    } else if (otherUserIdObj instanceof Number) {
                        otherUserId = ((Number) otherUserIdObj).longValue();
                    }

                    if (otherUserId != null) {
                        UserDO otherUser = userMapper.selectById(otherUserId);
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
    public PageResult<MessageVO> getChatHistory(Long userId, Long targetUserId, Integer pageNum, Integer pageSize) {
        Page<MessageDO> page = new Page<>(pageNum, pageSize);

        // 查询两个用户之间的消息
        LambdaQueryWrapper<MessageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(MessageDO::getSenderId, userId).eq(MessageDO::getReceiverId, targetUserId))
                .or(w -> w.eq(MessageDO::getSenderId, targetUserId).eq(MessageDO::getReceiverId, userId))
                .orderByDesc(MessageDO::getCreatedAt);

        Page<MessageDO> messagePage = messageMapper.selectPage(page, wrapper);

        List<MessageVO> list = messagePage.getRecords().stream()
                .map(this::convertToMessageVO)
                .collect(Collectors.toList());

        return PageResult.of(list, messagePage.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(Long userId, Long targetUserId, String content, Integer type) {
        // 检查目标用户是否存在
        UserDO targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        MessageDO message = new MessageDO();
        message.setSenderId(userId);
        message.setReceiverId(targetUserId);
        message.setContent(content);
        message.setType(type != null ? type : 1);
        message.setReadStatus(false);

        messageMapper.insert(message);

        MessageVO response = convertToMessageVO(message);

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
    public PageResult<NotificationVO> getNotifications(Long userId, Integer type, Integer pageNum, Integer pageSize) {
        // 使用 system_notices 表获取通知（包含发送者信息）
        Page<SystemNoticeDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SystemNoticeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNoticeDO::getUserId, userId);

        if (type != null) {
            wrapper.eq(SystemNoticeDO::getNoticeType, type);
        }

        wrapper.orderByDesc(SystemNoticeDO::getCreatedAt);

        Page<SystemNoticeDO> noticePage = systemNoticeMapper.selectPage(page, wrapper);

        List<NotificationVO> list = noticePage.getRecords().stream()
                .map(this::convertSystemNoticeToVO)
                .collect(Collectors.toList());

        return PageResult.of(list, noticePage.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markNotificationAsRead(Long userId, Long notificationId) {
        NotificationDO notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new BusinessException(40402, "通知不存在");
        }

        notification.setReadStatus(true);
        notificationMapper.updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllNotificationsAsRead(Long userId, Integer type) {
        notificationMapper.markAllAsRead(userId, type);
    }

    @Override
    public Object getUnreadCount(Long userId) {
        UnreadCountVO response = new UnreadCountVO();

        // 统计未读私信数
        LambdaQueryWrapper<MessageDO> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(MessageDO::getReceiverId, userId)
                .eq(MessageDO::getReadStatus, false);
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
        NotificationDO notification = new NotificationDO();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setReadStatus(false);

        notificationMapper.insert(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemNotice(Long userId, Long fromUserId, Integer type, Long targetId, String content,
            String imageUrl) {
        SystemNoticeDO notice = new SystemNoticeDO();
        notice.setUserId(userId);
        notice.setFromUserId(fromUserId);
        notice.setNoticeType(type);
        notice.setTargetId(targetId);
        notice.setContent(content);
        notice.setImageUrl(imageUrl);
        notice.setReadStatus(0);

        systemNoticeMapper.insert(notice);
        log.info("发送系统通知: userId={}, fromUserId={}, type={}, targetId={}", userId, fromUserId, type, targetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shareNoteToUsers(Long userId, Long noteId, List<Long> userIds) {
        log.info("分享笔记: userId={}, noteId={}, userIds={}", userId, noteId, userIds);

        // 检查笔记是否存在
        NoteDO note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 1) {
            throw new BusinessException(40402, "笔记不存在");
        }

        // 获取分享者信息
        UserDO sender = userMapper.selectById(userId);
        if (sender == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        // 构建笔记数据JSON - 使用ObjectMapper确保正确转义
        String noteDataJson;
        try {
            Map<String, Object> noteDataMap = new HashMap<>();
            noteDataMap.put("noteId", note.getId());
            noteDataMap.put("title", note.getTitle() != null ? note.getTitle() : "");
            noteDataMap.put("coverImage", note.getCoverImage() != null ? note.getCoverImage() : "");

            // 截取内容，避免过长
            String content = note.getContent() != null ? note.getContent() : "";
            if (content.length() > 50) {
                content = content.substring(0, 50);
            }
            noteDataMap.put("content", content);

            noteDataJson = objectMapper.writeValueAsString(noteDataMap);
            log.info("笔记数据JSON: {}", noteDataJson);
        } catch (Exception e) {
            log.error("序列化笔记数据失败", e);
            throw new BusinessException(50001, "序列化笔记数据失败");
        }

        // 给每个用户发送消息
        for (Long targetUserId : userIds) {
            // 检查目标用户是否存在
            UserDO targetUser = userMapper.selectById(targetUserId);
            if (targetUser == null) {
                log.warn("目标用户不存在: userId={}", targetUserId);
                continue;
            }

            // 创建消息记录
            MessageDO message = new MessageDO();
            message.setSenderId(userId);
            message.setReceiverId(targetUserId);
            message.setContent("分享了一篇笔记");
            message.setType(4); // 4=笔记分享
            message.setReadStatus(false);
            message.setNoteData(noteDataJson);

            messageMapper.insert(message);
            log.info("笔记分享消息已创建: messageId={}, targetUserId={}", message.getId(), targetUserId);

            // 通过WebSocket实时推送消息
            try {
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("type", "private_message");

                Map<String, Object> messageData = new HashMap<>();
                messageData.put("id", message.getId());
                messageData.put("senderId", userId);
                messageData.put("receiverId", targetUserId);
                messageData.put("content", "分享了一篇笔记");
                messageData.put("messageType", 4);
                messageData.put("createdAt", message.getCreatedAt().toString());
                messageData.put("senderName", sender.getUsername());
                messageData.put("senderAvatar", sender.getAvatar());

                // 添加笔记数据
                Map<String, Object> noteData = new HashMap<>();
                noteData.put("noteId", note.getId());
                noteData.put("title", note.getTitle());
                noteData.put("coverImage", note.getCoverImage());
                noteData.put("content",
                        note.getContent() != null
                                ? note.getContent().substring(0, Math.min(50, note.getContent().length()))
                                : "");
                messageData.put("noteData", noteData);

                wsMessage.put("data", messageData);

                webSocketHandler.sendMessageToUser(targetUserId, wsMessage);
                log.info("笔记分享WebSocket消息已推送: targetUserId={}", targetUserId);
            } catch (Exception e) {
                log.error("WebSocket消息推送失败: targetUserId={}", targetUserId, e);
            }
        }

        log.info("笔记分享完成: noteId={}, 成功分享给{}个用户", noteId, userIds.size());
    }

    private int countUnreadNotifications(Long userId, Integer type) {
        LambdaQueryWrapper<SystemNoticeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNoticeDO::getUserId, userId)
                .eq(SystemNoticeDO::getNoticeType, type)
                .eq(SystemNoticeDO::getReadStatus, 0);
        return systemNoticeMapper.selectCount(wrapper).intValue();
    }

    private MessageVO convertToMessageVO(MessageDO message) {
        MessageVO response = new MessageVO();
        response.setId(message.getId().toString());
        response.setSenderId(message.getSenderId().toString());
        response.setReceiverId(message.getReceiverId().toString());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setReadStatus(message.getReadStatus());
        response.setCreatedAt(message.getCreatedAt().toString());
        response.setTimeAgo(TimeUtil.formatRelativeTime(message.getCreatedAt()));

        // 如果是笔记分享消息，添加noteData
        if (message.getType() != null && message.getType() == 4 && message.getNoteData() != null) {
            response.setNoteData(message.getNoteData());
        }

        // 如果是店铺分享消息，添加noteData（店铺数据也存储在noteData字段）
        if (message.getType() != null && message.getType() == 5 && message.getNoteData() != null) {
            response.setNoteData(message.getNoteData());
        }

        // 查询发送者信息
        UserDO sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            response.setSenderName(sender.getUsername());
            response.setSenderAvatar(sender.getAvatar());
        }

        return response;
    }

    private NotificationVO convertToNotificationVO(NotificationDO notification) {
        NotificationVO response = new NotificationVO();
        response.setId(notification.getId().toString());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setType(notification.getType());
        response.setRelatedId(notification.getRelatedId() != null ? notification.getRelatedId().toString() : null);
        response.setReadStatus(notification.getReadStatus());
        response.setCreatedAt(notification.getCreatedAt().toString());
        response.setTimeAgo(TimeUtil.formatRelativeTime(notification.getCreatedAt()));

        return response;
    }

    private NotificationVO convertSystemNoticeToVO(SystemNoticeDO notice) {
        NotificationVO response = new NotificationVO();
        response.setId(notice.getId().toString());
        response.setType(notice.getNoticeType());
        response.setContent(notice.getContent());
        response.setRelatedId(notice.getTargetId() != null ? notice.getTargetId().toString() : null);
        response.setReadStatus(notice.getReadStatus() != null && notice.getReadStatus() == 1);
        response.setCreatedAt(notice.getCreatedAt() != null ? notice.getCreatedAt().toString() : "");
        response.setTimeAgo(notice.getCreatedAt() != null ? TimeUtil.formatRelativeTime(notice.getCreatedAt()) : "");

        // 获取发送者信息
        if (notice.getFromUserId() != null) {
            UserDO fromUser = userMapper.selectById(notice.getFromUserId());
            if (fromUser != null) {
                response.setFromUserId(fromUser.getId());
                response.setFromUsername(fromUser.getUsername());
                response.setFromAvatar(fromUser.getAvatar());
            }
        }

        // 获取笔记信息（如果是点赞笔记或评论笔记类型）
        if (notice.getTargetId() != null && (notice.getNoticeType() == 1 || notice.getNoticeType() == 2)) {
            NoteDO note = noteMapper.selectById(notice.getTargetId());
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
            case 5:
                // AI审核通知特殊处理
                response.setTitle("AI审核助手");
                response.setFromUsername("AI审核助手");
                response.setFromAvatar("/static/icons/ai-assistant.png");
                break;
            default:
                response.setTitle("系统通知");
        }

        return response;
    }

    /**
     * AI审核助手的虚拟用户ID
     * 使用0表示系统用户/AI审核助手
     */
    private static final Long AI_AUDIT_ASSISTANT_ID = 0L;

    /**
     * AI审核通知类型
     */
    private static final int NOTICE_TYPE_AUDIT = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendAuditNotification(Long userId, String contentType, String contentTitle, String reason,
            String suggestion) {
        log.info("发送AI审核通知到通知列表: userId={}, contentType={}, reason={}", userId, contentType, reason);

        // 构建通知消息内容
        StringBuilder messageContent = new StringBuilder();
        messageContent.append("【内容审核通知】\n\n");
        messageContent.append("您发布的").append(contentType);
        if (contentTitle != null && !contentTitle.isEmpty()) {
            // 截取标题，避免过长
            String title = contentTitle.length() > 20 ? contentTitle.substring(0, 20) + "..." : contentTitle;
            messageContent.append("「").append(title).append("」");
        }
        messageContent.append("因违反社区规范已被屏蔽。\n\n");
        messageContent.append("📌 违规类型：").append(reason).append("\n\n");
        if (suggestion != null && !suggestion.isEmpty()) {
            messageContent.append("💡 整改建议：").append(suggestion).append("\n\n");
        }
        messageContent.append("如有疑问，请联系客服申诉。");

        // 创建系统通知记录 - 发送到通知Tab，类型为5（AI审核通知）
        SystemNoticeDO notice = new SystemNoticeDO();
        notice.setUserId(userId);
        notice.setFromUserId(AI_AUDIT_ASSISTANT_ID); // AI审核助手的虚拟用户ID
        notice.setNoticeType(NOTICE_TYPE_AUDIT); // 5=AI审核通知
        notice.setTargetId(null); // 审核通知不关联特定笔记/评论
        notice.setContent(messageContent.toString());
        notice.setImageUrl(null);
        notice.setReadStatus(0);

        systemNoticeMapper.insert(notice);
        log.info("AI审核通知已发送到通知列表: noticeId={}, userId={}", notice.getId(), userId);

        // 通过WebSocket实时推送通知给用户
        try {
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "system_notice");

            Map<String, Object> noticeData = new HashMap<>();
            noticeData.put("id", notice.getId());
            noticeData.put("noticeType", NOTICE_TYPE_AUDIT);
            noticeData.put("content", messageContent.toString());
            noticeData.put("fromUserId", AI_AUDIT_ASSISTANT_ID);
            noticeData.put("fromUsername", "AI审核助手");
            noticeData.put("fromAvatar", "/static/icons/ai-assistant.png");
            noticeData.put("createdAt", notice.getCreatedAt() != null ? notice.getCreatedAt().toString() : "");

            wsMessage.put("data", noticeData);

            webSocketHandler.sendMessageToUser(userId, wsMessage);
            log.info("AI审核WebSocket通知已推送: userId={}", userId);
        } catch (Exception e) {
            log.error("AI审核WebSocket通知推送失败: userId={}", userId, e);
            // 不影响通知发送，只是实时推送失败
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shareShopToUsers(Long userId, Long shopId, List<Long> userIds) {
        log.info("分享店铺: userId={}, shopId={}, userIds={}", userId, shopId, userIds);

        // 检查店铺是否存在
        com.businessreviews.model.dataobject.ShopDO shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getStatus() != 1) {
            throw new BusinessException(40402, "店铺不存在");
        }

        // 获取分享者信息
        UserDO sender = userMapper.selectById(userId);
        if (sender == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        // 构建店铺数据JSON
        String shopDataJson;
        try {
            Map<String, Object> shopDataMap = new HashMap<>();
            shopDataMap.put("shopId", shop.getId());
            shopDataMap.put("name", shop.getName() != null ? shop.getName() : "");
            shopDataMap.put("headerImage", shop.getHeaderImage() != null ? shop.getHeaderImage() : "");
            shopDataMap.put("rating", shop.getRating() != null ? shop.getRating() : 0);
            shopDataMap.put("reviewCount", shop.getReviewCount() != null ? shop.getReviewCount() : 0);
            shopDataMap.put("address", shop.getAddress() != null ? shop.getAddress() : "");

            shopDataJson = objectMapper.writeValueAsString(shopDataMap);
            log.info("店铺数据JSON: {}", shopDataJson);
        } catch (Exception e) {
            log.error("序列化店铺数据失败", e);
            throw new BusinessException(50001, "序列化店铺数据失败");
        }

        // 给每个用户发送消息
        for (Long targetUserId : userIds) {
            // 检查目标用户是否存在
            UserDO targetUser = userMapper.selectById(targetUserId);
            if (targetUser == null) {
                log.warn("目标用户不存在: userId={}", targetUserId);
                continue;
            }

            // 创建消息记录
            MessageDO message = new MessageDO();
            message.setSenderId(userId);
            message.setReceiverId(targetUserId);
            message.setContent("分享了一家店铺");
            message.setType(5); // 5=店铺分享
            message.setReadStatus(false);
            message.setNoteData(shopDataJson); // 复用noteData字段存储店铺数据

            messageMapper.insert(message);
            log.info("店铺分享消息已创建: messageId={}, targetUserId={}", message.getId(), targetUserId);

            // 通过WebSocket实时推送消息
            try {
                Map<String, Object> wsMessage = new HashMap<>();
                wsMessage.put("type", "private_message");

                Map<String, Object> messageData = new HashMap<>();
                messageData.put("id", message.getId());
                messageData.put("senderId", userId);
                messageData.put("receiverId", targetUserId);
                messageData.put("content", "分享了一家店铺");
                messageData.put("messageType", 5);
                messageData.put("createdAt", message.getCreatedAt().toString());
                messageData.put("senderName", sender.getUsername());
                messageData.put("senderAvatar", sender.getAvatar());

                // 添加店铺数据
                Map<String, Object> shopData = new HashMap<>();
                shopData.put("shopId", shop.getId());
                shopData.put("name", shop.getName());
                shopData.put("headerImage", shop.getHeaderImage());
                shopData.put("rating", shop.getRating());
                shopData.put("reviewCount", shop.getReviewCount());
                shopData.put("address", shop.getAddress());
                messageData.put("shopData", shopData);

                wsMessage.put("data", messageData);

                webSocketHandler.sendMessageToUser(targetUserId, wsMessage);
                log.info("店铺分享WebSocket消息已推送: targetUserId={}", targetUserId);
            } catch (Exception e) {
                log.error("WebSocket消息推送失败: targetUserId={}", targetUserId, e);
            }
        }

        log.info("店铺分享完成: shopId={}, 成功分享给{}个用户", shopId, userIds.size());
    }
}
