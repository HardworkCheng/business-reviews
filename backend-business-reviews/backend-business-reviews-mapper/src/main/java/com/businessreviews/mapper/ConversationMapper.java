package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 会话Mapper
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    
    /**
     * 增加未读消息数
     */
    @Update("UPDATE conversations SET user1_unread_count = user1_unread_count + 1 WHERE id = #{conversationId} AND user1_id = #{userId}")
    int incrementUser1UnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    @Update("UPDATE conversations SET user2_unread_count = user2_unread_count + 1 WHERE id = #{conversationId} AND user2_id = #{userId}")
    int incrementUser2UnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    /**
     * 清空未读消息数
     */
    @Update("UPDATE conversations SET user1_unread_count = 0 WHERE id = #{conversationId} AND user1_id = #{userId}")
    int clearUser1UnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    @Update("UPDATE conversations SET user2_unread_count = 0 WHERE id = #{conversationId} AND user2_id = #{userId}")
    int clearUser2UnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
