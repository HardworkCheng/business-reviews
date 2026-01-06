package com.businessreviews.web.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.model.vo.CommentVO;
import com.businessreviews.merchant.context.MerchantContext;
import com.businessreviews.service.merchant.MerchantCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家评论管理控制器
 * <p>
 * 提供商家对评论的管理功能：
 * - 查询评论列表、回复评论、删除评论
 * - 评论置顶、导出评论
 * - 评论统计、数据看板
 * </p>
 *
 * @author businessreviews
 */
@RestController
@RequestMapping("/merchant/comments")
@RequiredArgsConstructor
public class MerchantCommentController {

    private final MerchantCommentService merchantCommentService;

    /**
     * 获取评论列表
     *
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param shopId     门店ID
     * @param status     状态
     * @param keyword    关键词
     * @param isNegative 是否差评
     * @return 评论列表
     */
    @GetMapping
    public Result<PageResult<CommentVO>> getCommentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isNegative) {
        Long merchantId = MerchantContext.requireMerchantId();
        PageResult<CommentVO> result = merchantCommentService.getCommentList(
                merchantId, shopId, pageNum, pageSize, status, keyword, isNegative);
        return Result.success(result);
    }

    /**
     * 回复评论
     *
     * @param id      评论ID
     * @param request 回复内容
     * @return 成功结果
     */
    @PostMapping("/{id}/reply")
    public Result<?> replyComment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        String content = request.get("content");
        merchantCommentService.replyComment(merchantId, operatorId, id, content);
        return Result.success("回复成功");
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantCommentService.deleteComment(merchantId, operatorId, id);
        return Result.success("删除成功");
    }

    /**
     * 获取评论统计
     *
     * @return 统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getCommentStats() {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> stats = merchantCommentService.getCommentStats(merchantId);
        return Result.success(stats);
    }

    /**
     * 获取数据概览
     *
     * @param shopId 门店ID
     * @return 概览数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(@RequestParam(required = false) Long shopId) {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> dashboard = merchantCommentService.getDashboard(merchantId, shopId);
        return Result.success(dashboard);
    }

    /**
     * 置顶评论
     *
     * @param id      评论ID
     * @param request 置顶状态
     * @return 成功结果
     */
    @PutMapping("/{id}/top")
    public Result<?> topComment(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        Long merchantId = MerchantContext.requireMerchantId();
        Boolean isTop = request.get("isTop");
        merchantCommentService.topComment(merchantId, id, isTop);
        return Result.success("操作成功");
    }

    /**
     * 导出评论
     *
     * @param response 响应对象
     * @param shopId   门店ID
     * @param status   状态
     * @param keyword  关键词
     */
    @GetMapping("/export")
    public void exportComments(
            jakarta.servlet.http.HttpServletResponse response,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Long merchantId = MerchantContext.requireMerchantId();
        merchantCommentService.exportComments(merchantId, response, shopId, status, keyword);
    }
}
