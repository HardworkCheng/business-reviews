package com.businessreviews.controller;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.dto.request.AddCommentRequest;
import com.businessreviews.dto.response.CommentResponse;
import com.businessreviews.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取笔记评论列表
     */
    @GetMapping("/notes/{noteId}/comments")
    public Result<PageResult<CommentResponse>> getNoteComments(
            @PathVariable Long noteId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<CommentResponse> result = commentService.getNoteComments(noteId, userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取评论的回复列表
     */
    @GetMapping("/comments/{commentId}/replies")
    public Result<PageResult<CommentResponse>> getCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<CommentResponse> result = commentService.getCommentReplies(commentId, userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 添加评论
     */
    @PostMapping("/comments")
    public Result<CommentResponse> addComment(@RequestBody @Valid AddCommentRequest request) {
        Long userId = UserContext.requireUserId();
        CommentResponse response = commentService.addComment(userId, request);
        return Result.success("评论成功", response);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        commentService.deleteComment(userId, id);
        return Result.success("删除成功");
    }

    /**
     * 点赞评论
     */
    @PostMapping("/comments/{id}/like")
    public Result<Map<String, Integer>> likeComment(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        Integer likeCount = commentService.likeComment(userId, id);
        
        Map<String, Integer> data = new HashMap<>();
        data.put("likeCount", likeCount);
        return Result.success("点赞成功", data);
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/comments/{id}/like")
    public Result<Map<String, Integer>> unlikeComment(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        Integer likeCount = commentService.unlikeComment(userId, id);
        
        Map<String, Integer> data = new HashMap<>();
        data.put("likeCount", likeCount);
        return Result.success("取消点赞成功", data);
    }
}
