package com.businessreviews.web.app;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.model.dto.app.PublishNoteDTO;
import com.businessreviews.model.vo.NoteDetailVO;
import com.businessreviews.model.vo.NoteItemVO;
import com.businessreviews.service.app.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 移动端笔记控制器 (UniApp)
 * 
 * 提供移动端用户的笔记相关API：
 * - GET /notes/recommended - 获取推荐笔记列表
 * - GET /notes/my - 获取我的笔记列表
 * - GET /notes/liked - 获取我点赞的笔记
 * - GET /notes/explore - 获取探索页笔记
 * - GET /notes/nearby - 获取附近笔记
 * - GET /notes/following - 获取关注用户的笔记
 * - GET /notes/{id} - 获取笔记详情
 * - POST /notes - 发布笔记
 * - PUT /notes/{id} - 更新笔记
 * - DELETE /notes/{id} - 删除笔记
 * - POST /notes/{id}/like - 点赞笔记
 * - POST /notes/{id}/bookmark - 收藏笔记
 * 
 * @see com.businessreviews.service.NoteService
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * 获取推荐笔记列表
     */
    @GetMapping("/recommended")
    public Result<PageResult<NoteItemVO>> getRecommendedNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NoteItemVO> result = noteService.getRecommendedNotes(pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取我的笔记列表
     */
    @GetMapping("/my")
    public Result<PageResult<NoteItemVO>> getMyNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<NoteItemVO> result = noteService.getUserNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }


    /**
     * 获取我点赞的笔记列表
     */
    @GetMapping("/liked")
    public Result<PageResult<NoteItemVO>> getLikedNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<NoteItemVO> result = noteService.getLikedNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取指定用户的笔记列表
     */
    @GetMapping("/user/{userId}")
    public Result<PageResult<NoteItemVO>> getUserNotes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NoteItemVO> result = noteService.getUserNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取探索页笔记列表
     */
    @GetMapping("/explore")
    public Result<PageResult<NoteItemVO>> getExploreNotes(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NoteItemVO> result = noteService.getExploreNotes(categoryId, sortBy, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取附近笔记列表
     */
    @GetMapping("/nearby")
    public Result<PageResult<NoteItemVO>> getNearbyNotes(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double distance,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NoteItemVO> result = noteService.getNearbyNotes(latitude, longitude, distance, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取关注用户的笔记列表
     */
    @GetMapping("/following")
    public Result<PageResult<NoteItemVO>> getFollowingNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.success(PageResult.empty(pageNum, pageSize));
        }
        PageResult<NoteItemVO> result = noteService.getFollowingNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取笔记详情
     */
    @GetMapping("/{id}")
    public Result<NoteDetailVO> getNoteDetail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        NoteDetailVO response = noteService.getNoteDetail(id, userId);
        
        // 异步增加浏览量并记录浏览历史
        noteService.increaseViewCount(id, userId);
        
        return Result.success(response);
    }

    /**
     * 发布笔记
     */
    @PostMapping
    public Result<Map<String, String>> publishNote(@RequestBody @Valid PublishNoteDTO request) {
        Long userId = UserContext.requireUserId();
        Long noteId = noteService.publishNote(userId, request);
        
        Map<String, String> data = new HashMap<>();
        data.put("noteId", noteId.toString());
        return Result.success("发布成功", data);
    }

    /**
     * 更新笔记
     */
    @PutMapping("/{id}")
    public Result<?> updateNote(@PathVariable Long id, @RequestBody @Valid PublishNoteDTO request) {
        Long userId = UserContext.requireUserId();
        noteService.updateNote(userId, id, request);
        return Result.success("更新成功");
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteNote(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        noteService.deleteNote(userId, id);
        return Result.success("删除成功");
    }

    /**
     * 点赞笔记（切换状态）
     */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> likeNote(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        Integer likeCount = noteService.likeNote(userId, id);
        boolean isLiked = noteService.isLiked(userId, id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("likeCount", likeCount);
        data.put("isLiked", isLiked);
        return Result.success(isLiked ? "点赞成功" : "取消点赞成功", data);
    }

    /**
     * 取消点赞笔记
     */
    @DeleteMapping("/{id}/like")
    public Result<Map<String, Integer>> unlikeNote(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        Integer likeCount = noteService.unlikeNote(userId, id);
        
        Map<String, Integer> data = new HashMap<>();
        data.put("likeCount", likeCount);
        return Result.success("取消点赞成功", data);
    }

    /**
     * 收藏笔记（切换状态）
     */
    @PostMapping("/{id}/bookmark")
    public Result<Map<String, Object>> bookmarkNote(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        noteService.bookmarkNote(userId, id);
        boolean isBookmarked = noteService.isBookmarked(userId, id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("isBookmarked", isBookmarked);
        return Result.success(isBookmarked ? "收藏成功" : "取消收藏成功", data);
    }

    /**
     * 取消收藏笔记
     */
    @DeleteMapping("/{id}/bookmark")
    public Result<?> unbookmarkNote(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        noteService.unbookmarkNote(userId, id);
        return Result.success("取消收藏成功");
    }
}
