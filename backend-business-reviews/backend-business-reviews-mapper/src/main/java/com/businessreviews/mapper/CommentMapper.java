package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Update("UPDATE comments SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);
    
    @Update("UPDATE comments SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    int decrementLikeCount(@Param("id") Long id);
    
    @Update("UPDATE comments SET reply_count = reply_count + 1 WHERE id = #{id}")
    int incrementReplyCount(@Param("id") Long id);
    
    @Update("UPDATE comments SET reply_count = reply_count - 1 WHERE id = #{id} AND reply_count > 0")
    int decrementReplyCount(@Param("id") Long id);
}
