package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {
    
    @Select("SELECT * FROM user_stats WHERE user_id = #{userId}")
    UserStats selectByUserId(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET following_count = following_count + 1 WHERE user_id = #{userId}")
    int incrementFollowingCount(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET following_count = following_count - 1 WHERE user_id = #{userId} AND following_count > 0")
    int decrementFollowingCount(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET follower_count = follower_count + 1 WHERE user_id = #{userId}")
    int incrementFollowerCount(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET follower_count = follower_count - 1 WHERE user_id = #{userId} AND follower_count > 0")
    int decrementFollowerCount(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET note_count = note_count + 1 WHERE user_id = #{userId}")
    int incrementNoteCount(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET note_count = note_count - 1 WHERE user_id = #{userId} AND note_count > 0")
    int decrementNoteCount(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET like_count = like_count + 1 WHERE user_id = #{userId}")
    int incrementLikeCount(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET like_count = like_count - 1 WHERE user_id = #{userId} AND like_count > 0")
    int decrementLikeCount(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET favorite_count = favorite_count + 1 WHERE user_id = #{userId}")
    int incrementFavoriteCount(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET favorite_count = favorite_count - 1 WHERE user_id = #{userId} AND favorite_count > 0")
    int decrementFavoriteCount(@Param("userId") Long userId);
}
