package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.CommentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 评论Mapper接口
 * 合并自 CommentMapper 和 CommentDOMapper
 * 
 * @author businessreviews
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentDO> {
    
    @Update("UPDATE comments SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);
    
    @Update("UPDATE comments SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    int decrementLikeCount(@Param("id") Long id);
    
    @Update("UPDATE comments SET reply_count = reply_count + 1 WHERE id = #{id}")
    int incrementReplyCount(@Param("id") Long id);
    
    @Update("UPDATE comments SET reply_count = reply_count - 1 WHERE id = #{id} AND reply_count > 0")
    int decrementReplyCount(@Param("id") Long id);
    
    /**
     * 根据笔记ID查询评论列表
     * 
     * @param noteId 笔记ID
     * @return 评论DO列表
     */
    @Select("SELECT * FROM note_comments WHERE note_id = #{noteId} AND status = 1 ORDER BY created_at DESC")
    List<CommentDO> listByNoteId(@Param("noteId") Long noteId);
}
