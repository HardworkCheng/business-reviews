package com.businessreviews.web.app;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.model.dto.app.AddCommentDTO;
import com.businessreviews.model.vo.CommentVO;
import com.businessreviews.service.app.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 移动端评论控制器 (UniApp)
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/notes/{noteId}/comments")
    public Result<PageResult<CommentVO>> getNoteComments(
            @PathVariable Long noteId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<CommentVO> result = commentService.getNoteComments(noteId, userId, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/comments/{commentId}/replies")
    public Result<PageResult<CommentVO>> getCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<CommentVO> result = commentService.getCommentReplies(commentId, userId, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/comments")
    public Result<CommentVO> addComment(@RequestBody @Valid AddCommentDTO request) {
        Long userId = UserContext.requireUserId();
        CommentVO response = commentService.addComment(userId, request);
        return Result.success("评论成功", response);
    }

    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        commentService.deleteComment(userId, id);
        return Result.success("删除成功");
    }

    @PostMapping("/comments/{id}/like")
    public Result<Map<String, Integer>> likeComment(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        Integer likeCount = commentService.likeComment(userId, id);
        
        Map<String, Integer> data = new HashMap<>();
        data.put("likeCount", likeCount);
        return Result.success("点赞成功", data);
    }

    @DeleteMapping("/comments/{id}/like")
    public Result<Map<String, Integer>> unlikeComment(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        Integer likeCount = commentService.unlikeComment(userId, id);
        
        Map<String, Integer> data = new HashMap<>();
        data.put("likeCount", likeCount);
        return Result.success("取消点赞成功", data);
    }
}
