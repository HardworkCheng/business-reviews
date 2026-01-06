package com.businessreviews.service.app;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.ConversationVO;
import com.businessreviews.model.vo.MessageVO;
import com.businessreviews.model.vo.NotificationVO;
import com.businessreviews.model.dataobject.MessageDO;

/**
 * 用户端消息服务接口
 * <p>
 * 提供UniApp移动端的私信、系统通知等功能
 * </p>
 * 
 * @author businessreviews
 */
public interface MessageService extends IService<MessageDO> {

    /**
     * 获取会话列表
     * 
     * @param userId   当前用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 会话列表分页数据
     */
    PageResult<ConversationVO> getConversations(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取聊天记录
     * 
     * @param userId       当前用户ID
     * @param targetUserId 对方用户ID
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @return 消息列表分页数据
     */
    PageResult<MessageVO> getChatHistory(Long userId, Long targetUserId, Integer pageNum, Integer pageSize);

    /**
     * 发送消息
     * 
     * @param userId       发送者ID
     * @param targetUserId 接收者ID
     * @param content      消息内容
     * @param type         消息类型（1文本，2图片，3语音）
     * @return 发送成功的消息VO
     */
    MessageVO sendMessage(Long userId, Long targetUserId, String content, Integer type);

    /**
     * 标记消息已读
     * 
     * @param userId       当前用户ID
     * @param targetUserId 对方用户ID
     */
    void markAsRead(Long userId, Long targetUserId);

    /**
     * 获取系统通知列表
     * 
     * @param userId   当前用户ID
     * @param type     通知类型（0全部，1点赞，2评论，3关注，4系统）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 通知列表分页数据
     */
    PageResult<NotificationVO> getNotifications(Long userId, Integer type, Integer pageNum, Integer pageSize);

    /**
     * 标记通知已读
     * 
     * @param userId         当前用户ID
     * @param notificationId 通知ID
     */
    void markNotificationAsRead(Long userId, Long notificationId);

    /**
     * 标记所有通知已读
     * 
     * @param userId 当前用户ID
     * @param type   通知类型（0全部）
     */
    void markAllNotificationsAsRead(Long userId, Integer type);

    /**
     * 获取未读消息数量
     * 
     * @param userId 当前用户ID
     * @return 未读数统计对象
     */
    Object getUnreadCount(Long userId);

    /**
     * 发送系统通知
     * 
     * @param userId    接收者用户ID
     * @param title     通知标题
     * @param content   通知内容
     * @param type      通知类型
     * @param relatedId 关联ID
     */
    void sendNotification(Long userId, String title, String content, Integer type, Long relatedId);

    /**
     * 发送系统通知（包含发送者信息）
     * 
     * @param userId     接收通知的用户ID
     * @param fromUserId 触发通知的用户ID
     * @param type       通知类型（1点赞笔记，2评论笔记，3关注，4点赞评论）
     * @param targetId   目标ID（笔记ID或评论ID）
     * @param content    通知内容
     * @param imageUrl   关联图片URL
     */
    void sendSystemNotice(Long userId, Long fromUserId, Integer type, Long targetId, String content, String imageUrl);

    /**
     * 分享笔记给多个用户
     * 
     * @param userId  分享者用户ID
     * @param noteId  笔记ID
     * @param userIds 接收者用户ID列表
     */
    void shareNoteToUsers(Long userId, Long noteId, java.util.List<Long> userIds);

    /**
     * 发送AI审核助手通知
     * 当用户发布的内容被AI审核系统屏蔽时，向用户发送通知
     * 
     * @param userId       接收通知的用户ID
     * @param contentType  内容类型（"笔记" 或 "评论"）
     * @param contentTitle 内容标题或摘要
     * @param reason       屏蔽原因
     * @param suggestion   整改建议
     */
    void sendAuditNotification(Long userId, String contentType, String contentTitle, String reason, String suggestion);

    /**
     * 分享店铺给多个用户
     * 
     * @param userId  分享者用户ID
     * @param shopId  店铺ID
     * @param userIds 接收者用户ID列表
     */
    void shareShopToUsers(Long userId, Long shopId, java.util.List<Long> userIds);
}
