package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    /**
     * 获取会话列表 - 获取每个会话的最新消息
     * 兼容 MySQL only_full_group_by 模式
     */
    @Select("SELECT " +
            "  latest.other_user_id, " +
            "  m.content as last_message, " +
            "  m.created_at as last_time, " +
            "  COALESCE(unread.unread_count, 0) as unread_count " +
            "FROM ( " +
            "  SELECT " +
            "    CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END as other_user_id, " +
            "    MAX(id) as max_id " +
            "  FROM messages " +
            "  WHERE sender_id = #{userId} OR receiver_id = #{userId} " +
            "  GROUP BY CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END " +
            ") latest " +
            "INNER JOIN messages m ON m.id = latest.max_id " +
            "LEFT JOIN ( " +
            "  SELECT sender_id, COUNT(*) as unread_count " +
            "  FROM messages " +
            "  WHERE receiver_id = #{userId} AND is_read = 0 " +
            "  GROUP BY sender_id " +
            ") unread ON latest.other_user_id = unread.sender_id " +
            "ORDER BY m.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectConversations(@Param("userId") Long userId, 
                                                   @Param("offset") int offset, 
                                                   @Param("limit") int limit);
    
    @Select("SELECT COUNT(DISTINCT CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END) " +
            "FROM messages WHERE sender_id = #{userId} OR receiver_id = #{userId}")
    Long countConversations(@Param("userId") Long userId);
    
    @Update("UPDATE messages SET is_read = 1 " +
            "WHERE receiver_id = #{userId} AND sender_id = #{targetUserId} AND is_read = 0")
    int markAsRead(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);
}
