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
 * <p>
 * 处理移动端笔记评论的查询、发表、回复、删除及点赞操作。
 * </p>
 *
 * @author businessreviews
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取笔记评论列表
     * <p>
     * 分页查询指定笔记的一级评论。
     * </p>
     *
     * @param noteId   笔记ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 评论列表
     */
    @GetMapping("/notes/{noteId}/comments")
    public Result<PageResult<CommentVO>> getNoteComments(
            @PathVariable Long noteId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<CommentVO> result = commentService.getNoteComments(noteId, userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取评论回复列表
     *
     * @param commentId 评论ID
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @return 回复评论列表
     */
    @GetMapping("/comments/{commentId}/replies")
    public Result<PageResult<CommentVO>> getCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<CommentVO> result = commentService.getCommentReplies(commentId, userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 发表评论
     *
     * @param request 评论内容参数
     * @return 新评论信息
     */
    @PostMapping("/comments")
    public Result<CommentVO> addComment(@RequestBody @Valid AddCommentDTO request) {
        Long userId = UserContext.requireUserId();
        CommentVO response = commentService.addComment(userId, request);
        return Result.success("评论成功", response);
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @return 成功结果
     */
    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        commentService.deleteComment(userId, id);
        return Result.success("删除成功");
    }

    /**
     * 点赞评论
     *
     * @param id 评论ID
     * @return 更新后的点赞数
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
     *
     * @param id 评论ID
     * @return 更新后的点赞数
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
