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
     * 
     * @param isNegative 是否只查询差评（评分<3分）
     */
    PageResult<CommentVO> getCommentList(Long merchantId, Long shopId, Integer pageNum, Integer pageSize,
            Integer status, String keyword, Boolean isNegative);

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

    /**
     * 获取数据概览
     */
    Map<String, Object> getDashboard(Long merchantId, Long shopId);

    /**
     * 置顶评论
     */
    /**
     * 置顶评论
     */
    void topComment(Long merchantId, Long commentId, Boolean isTop);

    /**
     * 导出评论数据
     */
    void exportComments(Long merchantId, jakarta.servlet.http.HttpServletResponse response, Long shopId, Integer status,
            String keyword);
}
