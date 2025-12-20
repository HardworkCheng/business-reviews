package com.businessreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.PublishNoteDTO;
import com.businessreviews.model.vo.NoteDetailVO;
import com.businessreviews.model.vo.NoteItemVO;
import com.businessreviews.model.dataobject.NoteDO;

import java.util.List;

public interface NoteService extends IService<NoteDO> {
    
    /**
     * 获取推荐笔记列表
     */
    PageResult<NoteItemVO> getRecommendedNotes(Integer pageNum, Integer pageSize);
    
    /**
     * 获取用户笔记列表
     */
    PageResult<NoteItemVO> getUserNotes(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取用户点赞的笔记列表
     */
    PageResult<NoteItemVO> getLikedNotes(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取探索页笔记列表
     */
    PageResult<NoteItemVO> getExploreNotes(Long categoryId, String sortBy, Integer pageNum, Integer pageSize);
    
    /**
     * 获取附近笔记列表
     */
    PageResult<NoteItemVO> getNearbyNotes(Double latitude, Double longitude, Double distance, Integer pageNum, Integer pageSize);
    
    /**
     * 获取关注用户的笔记列表
     */
    PageResult<NoteItemVO> getFollowingNotes(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取笔记详情
     */
    NoteDetailVO getNoteDetail(Long noteId, Long userId);
    
    /**
     * 发布笔记
     */
    Long publishNote(Long userId, PublishNoteDTO request);
    
    /**
     * 更新笔记
     */
    void updateNote(Long userId, Long noteId, PublishNoteDTO request);
    
    /**
     * 删除笔记
     */
    void deleteNote(Long userId, Long noteId);
    
    /**
     * 点赞笔记
     */
    Integer likeNote(Long userId, Long noteId);
    
    /**
     * 取消点赞笔记
     */
    Integer unlikeNote(Long userId, Long noteId);
    
    /**
     * 收藏笔记
     */
    void bookmarkNote(Long userId, Long noteId);
    
    /**
     * 取消收藏笔记
     */
    void unbookmarkNote(Long userId, Long noteId);
    
    /**
     * 增加浏览量并记录浏览历史
     */
    void increaseViewCount(Long noteId, Long userId);
    
    /**
     * 检查是否已点赞
     */
    boolean isLiked(Long userId, Long noteId);
    
    /**
     * 检查是否已收藏
     */
    boolean isBookmarked(Long userId, Long noteId);
    
    /**
     * 搜索笔记
     */
    PageResult<NoteItemVO> searchNotes(String keyword, Integer pageNum, Integer pageSize);
}
