package com.businessreviews.service.app;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.app.AddCommentDTO;
import com.businessreviews.model.vo.CommentVO;
import com.businessreviews.model.dataobject.CommentDO;

/**
 * 用户端评论服务接口
 * <p>
 * 提供UniApp移动端的评论发布、点赞、回复等功能
 * </p>
 * 
 * @author businessreviews
 */
public interface CommentService extends IService<CommentDO> {

    /**
     * 获取笔记评论列表
     * 
     * @param noteId   笔记ID
     * @param userId   当前用户ID（用于判断是否点赞）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 评论列表分页数据
     */
    PageResult<CommentVO> getNoteComments(Long noteId, Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取评论的回复列表
     * 
     * @param commentId 父评论ID
     * @param userId    当前用户ID
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 回复列表分页数据
     */
    PageResult<CommentVO> getCommentReplies(Long commentId, Long userId, Integer pageNum, Integer pageSize);

    /**
     * 添加评论
     * 
     * @param userId  发评人ID
     * @param request 评论内容请求对象
     * @return 新增评论的VO
     */
    CommentVO addComment(Long userId, AddCommentDTO request);

    /**
     * 删除评论
     * 
     * @param userId    操作人ID
     * @param commentId 待删除评论ID
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * 点赞评论
     * 
     * @param userId    点赞人ID
     * @param commentId 评论ID
     * @return 最新点赞数
     */
    Integer likeComment(Long userId, Long commentId);

    /**
     * 取消点赞评论
     * 
     * @param userId    操作人ID
     * @param commentId 评论ID
     * @return 最新点赞数
     */
    Integer unlikeComment(Long userId, Long commentId);

    /**
     * 检查是否已点赞评论
     * 
     * @param userId    用户ID
     * @param commentId 评论ID
     * @return true=已点赞，false=未点赞
     */
    boolean isCommentLiked(Long userId, Long commentId);
}
