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
 */
@RestController
@RequestMapping("/merchant/comments")
@RequiredArgsConstructor
public class MerchantCommentController {

    private final MerchantCommentService merchantCommentService;

    /**
     * 获取评论列表
     */
    @GetMapping
    public Result<PageResult<CommentVO>> getCommentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Long merchantId = MerchantContext.requireMerchantId();
        PageResult<CommentVO> result = merchantCommentService.getCommentList(
                merchantId, pageNum, pageSize, status, keyword);
        return Result.success(result);
    }

    /**
     * 回复评论
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
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getCommentStats() {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> stats = merchantCommentService.getCommentStats(merchantId);
        return Result.success(stats);
    }
}
