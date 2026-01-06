package com.businessreviews.service.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.CommentVO;
import java.util.Map;

/**
 * 商家评论服务接口
 * <p>
 * 提供商家运营中心的评论管理功能
 * </p>
 * 
 * @author businessreviews
 */
public interface MerchantCommentService {

        /**
         * 获取评论列表
         * 
         * @param merchantId 商家ID
         * @param shopId     店铺ID
         * @param pageNum    页码
         * @param pageSize   每页大小
         * @param status     状态过滤
         * @param keyword    关键词过滤
         * @param isNegative 是否只查询差评（评分<3分）
         * @return 评论列表分页数据
         */
        PageResult<CommentVO> getCommentList(Long merchantId, Long shopId, Integer pageNum, Integer pageSize,
                        Integer status, String keyword, Boolean isNegative);

        /**
         * 回复评论
         * 
         * @param merchantId 商家ID
         * @param operatorId 操作员ID
         * @param commentId  评论ID
         * @param content    回复内容
         */
        void replyComment(Long merchantId, Long operatorId, Long commentId, String content);

        /**
         * 删除评论
         * 
         * @param merchantId 商家ID
         * @param operatorId 操作员ID
         * @param commentId  评论ID
         */
        void deleteComment(Long merchantId, Long operatorId, Long commentId);

        /**
         * 获取评论统计
         * 
         * @param merchantId 商家ID
         * @return 统计数据MAP
         */
        Map<String, Object> getCommentStats(Long merchantId);

        /**
         * 获取数据概览
         * 
         * @param merchantId 商家ID
         * @param shopId     店铺ID
         * @return 概览数据MAP
         */
        Map<String, Object> getDashboard(Long merchantId, Long shopId);

        /**
         * 置顶评论
         * 
         * @param merchantId 商家ID
         * @param commentId  评论ID
         * @param isTop      是否置顶（true=置顶, false=取消置顶）
         */
        void topComment(Long merchantId, Long commentId, Boolean isTop);

        /**
         * 导出评论数据
         * 
         * @param merchantId 商家ID
         * @param response   HTTP响应对象
         * @param shopId     店铺ID
         * @param status     状态过滤
         * @param keyword    关键词过滤
         */
        void exportComments(Long merchantId, jakarta.servlet.http.HttpServletResponse response, Long shopId,
                        Integer status,
                        String keyword);
}
