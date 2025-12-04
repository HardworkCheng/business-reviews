package com.businessreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.request.AddCommentRequest;
import com.businessreviews.dto.response.CommentResponse;
import com.businessreviews.entity.Comment;

public interface CommentService extends IService<Comment> {
    
    /**
     * 获取笔记评论列表
     */
    PageResult<CommentResponse> getNoteComments(Long noteId, Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取评论的回复列表
     */
    PageResult<CommentResponse> getCommentReplies(Long commentId, Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 添加评论
     */
    CommentResponse addComment(Long userId, AddCommentRequest request);
    
    /**
     * 删除评论
     */
    void deleteComment(Long userId, Long commentId);
    
    /**
     * 点赞评论
     */
    Integer likeComment(Long userId, Long commentId);
    
    /**
     * 取消点赞评论
     */
    Integer unlikeComment(Long userId, Long commentId);
    
    /**
     * 检查是否已点赞评论
     */
    boolean isCommentLiked(Long userId, Long commentId);
}
