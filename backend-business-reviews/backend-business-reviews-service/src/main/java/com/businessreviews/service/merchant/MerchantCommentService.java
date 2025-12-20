package com.businessreviews.service.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.CommentVO;
import java.util.Map;

/**
 * 商家评论服务接口
 * 提供商家运营中心的评论管理功能
 */
public interface MerchantCommentService {
    
    /**
     * 获取评论列表
     */
    PageResult<CommentVO> getCommentList(Long merchantId, Integer pageNum, Integer pageSize, 
            Integer status, String keyword);
    
    /**
     * 回复评论
     */
    void replyComment(Long merchantId, Long operatorId, Long commentId, String content);
    
    /**
     * 删除评论
     */
    void deleteComment(Long merchantId, Long operatorId, Long commentId);
    
    /**
     * 获取评论统计
     */
    Map<String, Object> getCommentStats(Long merchantId);
}
