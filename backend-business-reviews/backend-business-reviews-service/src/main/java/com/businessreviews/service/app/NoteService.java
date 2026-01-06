package com.businessreviews.service.app;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.app.PublishNoteDTO;
import com.businessreviews.model.vo.NoteDetailVO;
import com.businessreviews.model.vo.NoteItemVO;
import com.businessreviews.model.dataobject.NoteDO;

/**
 * 用户端笔记服务接口
 * <p>
 * 提供UniApp移动端的笔记发布、浏览、点赞、收藏等功能
 * </p>
 * 
 * @author businessreviews
 */
public interface NoteService extends IService<NoteDO> {

    /**
     * 获取推荐笔记列表
     * 
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 推荐笔记列表分页数据
     */
    PageResult<NoteItemVO> getRecommendedNotes(Integer pageNum, Integer pageSize);

    /**
     * 获取用户笔记列表
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 用户笔记列表分页数据
     */
    PageResult<NoteItemVO> getUserNotes(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户点赞的笔记列表
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 点赞笔记列表分页数据
     */
    PageResult<NoteItemVO> getLikedNotes(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取探索页笔记列表
     * 
     * @param categoryId 分类ID
     * @param sortBy     排序方式
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 探索页笔记列表分页数据
     */
    PageResult<NoteItemVO> getExploreNotes(Long categoryId, String sortBy, Integer pageNum, Integer pageSize);

    /**
     * 获取附近笔记列表
     * 
     * @param latitude  纬度
     * @param longitude 经度
     * @param distance  距离范围（千米）
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 附近笔记列表分页数据
     */
    PageResult<NoteItemVO> getNearbyNotes(Double latitude, Double longitude, Double distance, Integer pageNum,
            Integer pageSize);

    /**
     * 获取关注用户的笔记列表
     * 
     * @param userId   当前用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 关注用户笔记列表分页数据
     */
    PageResult<NoteItemVO> getFollowingNotes(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取笔记详情
     * 
     * @param noteId 笔记ID
     * @param userId 当前用户ID（用于判断点赞收藏状态）
     * @return 笔记详情VO
     */
    NoteDetailVO getNoteDetail(Long noteId, Long userId);

    /**
     * 发布笔记
     * 
     * @param userId  发布人ID
     * @param request 笔记发布请求对象
     * @return 新增笔记ID
     */
    Long publishNote(Long userId, PublishNoteDTO request);

    /**
     * 更新笔记
     * 
     * @param userId  操作人ID
     * @param noteId  笔记ID
     * @param request 笔记更新请求对象
     */
    void updateNote(Long userId, Long noteId, PublishNoteDTO request);

    /**
     * 删除笔记
     * 
     * @param userId 操作人ID
     * @param noteId 笔记ID
     */
    void deleteNote(Long userId, Long noteId);

    /**
     * 点赞笔记
     * 
     * @param userId 点赞人ID
     * @param noteId 笔记ID
     * @return 最新点赞数
     */
    Integer likeNote(Long userId, Long noteId);

    /**
     * 取消点赞笔记
     * 
     * @param userId 操作人ID
     * @param noteId 笔记ID
     * @return 最新点赞数
     */
    Integer unlikeNote(Long userId, Long noteId);

    /**
     * 收藏笔记
     * 
     * @param userId 收藏人ID
     * @param noteId 笔记ID
     */
    void bookmarkNote(Long userId, Long noteId);

    /**
     * 取消收藏笔记
     * 
     * @param userId 操作人ID
     * @param noteId 笔记ID
     */
    void unbookmarkNote(Long userId, Long noteId);

    /**
     * 增加浏览量并记录浏览历史
     * 
     * @param noteId 笔记ID
     * @param userId 用户ID
     */
    void increaseViewCount(Long noteId, Long userId);

    /**
     * 检查是否已点赞
     * 
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return true=已点赞，false=未点赞
     */
    boolean isLiked(Long userId, Long noteId);

    /**
     * 检查是否已收藏
     * 
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return true=已收藏，false=未收藏
     */
    boolean isBookmarked(Long userId, Long noteId);

    /**
     * 搜索笔记
     * 
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 搜索结果笔记列表分页数据
     */
    PageResult<NoteItemVO> searchNotes(String keyword, Integer pageNum, Integer pageSize);
}
