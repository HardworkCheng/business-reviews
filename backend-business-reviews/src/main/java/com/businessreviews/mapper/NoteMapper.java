package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    
    @Update("UPDATE notes SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);
    
    @Update("UPDATE notes SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);
    
    @Update("UPDATE notes SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    int decrementLikeCount(@Param("id") Long id);
    
    @Update("UPDATE notes SET comment_count = comment_count + 1 WHERE id = #{id}")
    int incrementCommentCount(@Param("id") Long id);
    
    @Update("UPDATE notes SET comment_count = comment_count - 1 WHERE id = #{id} AND comment_count > 0")
    int decrementCommentCount(@Param("id") Long id);
    
    @Update("UPDATE notes SET favorite_count = favorite_count + 1 WHERE id = #{id}")
    int incrementFavoriteCount(@Param("id") Long id);
    
    @Update("UPDATE notes SET favorite_count = favorite_count - 1 WHERE id = #{id} AND favorite_count > 0")
    int decrementFavoriteCount(@Param("id") Long id);
}
