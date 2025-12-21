package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.NoteDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 笔记Mapper接口
 * 合并自 NoteMapper 和 NoteDOMapper
 * 
 * @author businessreviews
 */
@Mapper
public interface NoteMapper extends BaseMapper<NoteDO> {
    
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
    
    /**
     * 根据用户ID查询笔记列表
     * 
     * @param userId 用户ID
     * @return 笔记DO列表
     */
    @Select("SELECT * FROM notes WHERE user_id = #{userId} AND status = 1 ORDER BY created_at DESC")
    List<NoteDO> listByUserId(@Param("userId") Long userId);
    
    /**
     * 根据商家ID查询笔记列表
     * 
     * @param shopId 商家ID
     * @return 笔记DO列表
     */
    @Select("SELECT * FROM notes WHERE shop_id = #{shopId} AND status = 1 ORDER BY created_at DESC")
    List<NoteDO> listByShopId(@Param("shopId") Long shopId);
    
    /**
     * 查询推荐笔记列表
     * 
     * @return 推荐笔记DO列表
     */
    @Select("SELECT * FROM notes WHERE is_recommend = 1 AND status = 1 ORDER BY created_at DESC")
    List<NoteDO> listRecommended();
}
