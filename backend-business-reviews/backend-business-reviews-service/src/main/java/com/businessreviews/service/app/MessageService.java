package com.businessreviews.service.app;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.ConversationVO;
import com.businessreviews.model.vo.MessageVO;
import com.businessreviews.model.vo.NotificationVO;
import com.businessreviews.model.dataobject.MessageDO;

/**
 * 用户端消息服务接口
 * 提供UniApp移动端的私信、系统通知等功能
 */
public interface MessageService extends IService<MessageDO> {
    
    /**
     * 获取会话列表
     */
    PageResult<ConversationVO> getConversations(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取聊天记录
     */
    PageResult<MessageVO> getChatHistory(Long userId, Long targetUserId, Integer pageNum, Integer pageSize);
    
    /**
     * 发送消息
     */
    MessageVO sendMessage(Long userId, Long targetUserId, String content, Integer type);
    
    /**
     * 标记消息已读
     */
    void markAsRead(Long userId, Long targetUserId);
    
    /**
     * 获取系统通知列表
     */
    PageResult<NotificationVO> getNotifications(Long userId, Integer type, Integer pageNum, Integer pageSize);
    
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
    
    /**
     * 发送系统通知（包含发送者信息）
     * @param userId 接收通知的用户ID
     * @param fromUserId 触发通知的用户ID
     * @param type 通知类型（1点赞笔记，2评论笔记，3关注，4点赞评论）
     * @param targetId 目标ID（笔记ID或评论ID）
     * @param content 通知内容
     * @param imageUrl 关联图片URL
     */
    void sendSystemNotice(Long userId, Long fromUserId, Integer type, Long targetId, String content, String imageUrl);
    
    /**
     * 分享笔记给多个用户
     * @param userId 分享者用户ID
     * @param noteId 笔记ID
     * @param userIds 接收者用户ID列表
     */
    void shareNoteToUsers(Long userId, Long noteId, java.util.List<Long> userIds);
}
