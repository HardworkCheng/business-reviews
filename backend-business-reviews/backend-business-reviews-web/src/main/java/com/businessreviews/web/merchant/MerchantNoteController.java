package com.businessreviews.web.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.model.vo.NoteDetailVO;
import com.businessreviews.model.vo.NoteItemVO;
import com.businessreviews.merchant.context.MerchantContext;
import com.businessreviews.service.merchant.MerchantNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家运营中心 - 笔记管理控制器 (Web端)
 * <p>
 * 提供商家运营中心的笔记管理API：
 * - GET /merchant/notes - 获取笔记列表
 * - GET /merchant/notes/{id} - 获取笔记详情
 * - POST /merchant/notes - 创建笔记
 * - PUT /merchant/notes/{id} - 更新笔记
 * - POST /merchant/notes/{id}/publish - 发布笔记
 * - POST /merchant/notes/{id}/offline - 下线笔记
 * - DELETE /merchant/notes/{id} - 删除笔记
 * - GET /merchant/notes/{id}/stats - 获取笔记统计
 * </p>
 *
 * @author businessreviews
 * @see com.businessreviews.service.merchant.MerchantNoteService
 */
@RestController
@RequestMapping("/merchant/notes")
@RequiredArgsConstructor
public class MerchantNoteController {

    private final MerchantNoteService merchantNoteService;

    /**
     * 获取笔记列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param status   状态
     * @param shopId   门店ID
     * @param keyword  关键词
     * @return 笔记列表
     */
    @GetMapping
    public Result<PageResult<NoteItemVO>> getNoteList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String keyword) {
        Long merchantId = MerchantContext.requireMerchantId();
        PageResult<NoteItemVO> result = merchantNoteService.getNoteList(
                merchantId, pageNum, pageSize, status, shopId, keyword);
        return Result.success(result);
    }

    /**
     * 获取笔记详情
     *
     * @param id 笔记ID
     * @return 笔记详情
     */
    @GetMapping("/{id}")
    public Result<NoteDetailVO> getNoteDetail(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        NoteDetailVO response = merchantNoteService.getNoteDetail(merchantId, id);
        return Result.success(response);
    }

    /**
     * 创建笔记
     *
     * @param request 创建参数(title, content, type, etc.)
     * @return 创建结果(noteId)
     */
    @PostMapping
    public Result<Map<String, Long>> createNote(@RequestBody Map<String, Object> request) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        Long noteId = merchantNoteService.createNote(merchantId, operatorId, request);
        return Result.success("创建成功", Map.of("noteId", noteId));
    }

    /**
     * 更新笔记
     *
     * @param id      笔记ID
     * @param request 更新参数
     * @return 成功结果
     */
    @PutMapping("/{id}")
    public Result<?> updateNote(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantNoteService.updateNote(merchantId, operatorId, id, request);
        return Result.success("更新成功");
    }

    /**
     * 发布笔记
     *
     * @param id 笔记ID
     * @return 成功结果
     */
    @PostMapping("/{id}/publish")
    public Result<?> publishNote(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantNoteService.publishNote(merchantId, operatorId, id);
        return Result.success("发布成功");
    }

    /**
     * 下线笔记
     *
     * @param id 笔记ID
     * @return 成功结果
     */
    @PostMapping("/{id}/offline")
    public Result<?> offlineNote(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantNoteService.offlineNote(merchantId, operatorId, id);
        return Result.success("下线成功");
    }

    /**
     * 删除笔记
     *
     * @param id 笔记ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteNote(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        merchantNoteService.deleteNote(merchantId, operatorId, id);
        return Result.success("删除成功");
    }

    /**
     * 获取笔记统计
     *
     * @param id 笔记ID
     * @return 统计数据
     */
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getNoteStats(@PathVariable Long id) {
        Long merchantId = MerchantContext.requireMerchantId();
        Map<String, Object> stats = merchantNoteService.getNoteStats(merchantId, id);
        return Result.success(stats);
    }

    /**
     * 获取笔记评论列表
     *
     * @param id       笔记ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 评论列表
     */
    @GetMapping("/{id}/comments")
    public Result<PageResult<Map<String, Object>>> getNoteComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long merchantId = MerchantContext.requireMerchantId();
        PageResult<Map<String, Object>> result = merchantNoteService.getNoteComments(merchantId, id, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 商家发表评论
     *
     * @param id      笔记ID
     * @param request 评论参数
     * @return 评论结果
     */
    @PostMapping("/{id}/comments")
    public Result<Map<String, Long>> createNoteComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Long merchantId = MerchantContext.requireMerchantId();
        Long operatorId = MerchantContext.requireUserId();
        Long commentId = merchantNoteService.createNoteComment(merchantId, operatorId, id, request);
        return Result.success("评论成功", Map.of("commentId", commentId));
    }
}
