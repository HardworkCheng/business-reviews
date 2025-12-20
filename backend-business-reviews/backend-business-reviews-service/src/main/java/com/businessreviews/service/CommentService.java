package com.businessreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.AddCommentDTO;
import com.businessreviews.model.vo.CommentVO;
import com.businessreviews.model.dataobject.CommentDO;

public interface CommentService extends IService<CommentDO> {
    
    /**
     * 获取笔记评论列表
     */
    PageResult<CommentVO> getNoteComments(Long noteId, Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取评论的回复列表
     */
    PageResult<CommentVO> getCommentReplies(Long commentId, Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 添加评论
     */
    CommentVO addComment(Long userId, AddCommentDTO request);
    
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
