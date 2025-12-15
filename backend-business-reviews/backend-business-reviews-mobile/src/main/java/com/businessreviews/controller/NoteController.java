package com.businessreviews.controller;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.dto.request.PublishNoteRequest;
import com.businessreviews.dto.response.NoteDetailResponse;
import com.businessreviews.dto.response.NoteItemResponse;
import com.businessreviews.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * 获取推荐笔记列表
     */
    @GetMapping("/recommended")
    public Result<PageResult<NoteItemResponse>> getRecommendedNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NoteItemResponse> result = noteService.getRecommendedNotes(pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取我的笔记列表
     */
    @GetMapping("/my")
    public Result<PageResult<NoteItemResponse>> getMyNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<NoteItemResponse> result = noteService.getUserNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取我点赞的笔记列表
     */
    @GetMapping("/liked")
    public Result<PageResult<NoteItemResponse>> getLikedNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<NoteItemResponse> result = noteService.getLikedNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取指定用户的笔记列表
     */
    @GetMapping("/user/{userId}")
    public Result<PageResult<NoteItemResponse>> getUserNotes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NoteItemResponse> result = noteService.getUserNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取探索页笔记列表
     */
    @GetMapping("/explore")
    public Result<PageResult<NoteItemResponse>> getExploreNotes(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NoteItemResponse> result = noteService.getExploreNotes(categoryId, sortBy, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取附近笔记列表
     */
    @GetMapping("/nearby")
    public Result<PageResult<NoteItemResponse>> getNearbyNotes(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double distance,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NoteItemResponse> result = noteService.getNearbyNotes(latitude, longitude, distance, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取关注用户的笔记列表
     */
    @GetMapping("/following")
    public Result<PageResult<NoteItemResponse>> getFollowingNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.success(PageResult.empty(pageNum, pageSize));
        }
        PageResult<NoteItemResponse> result = noteService.getFollowingNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取笔记详情
     */
    @GetMapping("/{id}")
    public Result<NoteDetailResponse> getNoteDetail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        NoteDetailResponse response = noteService.getNoteDetail(id, userId);
        
        // 异步增加浏览量并记录浏览历史
        noteService.increaseViewCount(id, userId);
        
        return Result.success(response);
    }

    /**
     * 发布笔记
     */
    @PostMapping
    public Result<Map<String, String>> publishNote(@RequestBody @Valid PublishNoteRequest request) {
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
    public Result<?> updateNote(@PathVariable Long id, @RequestBody @Valid PublishNoteRequest request) {
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
