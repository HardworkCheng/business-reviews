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
    
    @Select("SELECT " +
            "CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END as other_user_id, " +
            "content as last_message, " +
            "created_at as last_time, " +
            "SUM(CASE WHEN receiver_id = #{userId} AND is_read = 0 THEN 1 ELSE 0 END) as unread_count " +
            "FROM messages " +
            "WHERE sender_id = #{userId} OR receiver_id = #{userId} " +
            "GROUP BY other_user_id " +
            "ORDER BY last_time DESC " +
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
