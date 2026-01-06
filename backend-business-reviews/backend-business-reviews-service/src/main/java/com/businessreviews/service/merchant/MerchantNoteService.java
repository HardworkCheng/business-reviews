package com.businessreviews.service.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.NoteItemVO;
import com.businessreviews.model.vo.NoteDetailVO;
import java.util.Map;

/**
 * 商家笔记服务接口
 * <p>
 * 提供商家运营中心的笔记管理功能
 * </p>
 * 
 * @author businessreviews
 */
public interface MerchantNoteService {

    /**
     * 获取笔记列表
     * 
     * @param merchantId 商家ID
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param status     状态过滤
     * @param shopId     店铺ID过滤
     * @param keyword    关键词过滤
     * @return 笔记列表分页数据
     */
    PageResult<NoteItemVO> getNoteList(Long merchantId, Integer pageNum, Integer pageSize,
            Integer status, Long shopId, String keyword);

    /**
     * 获取笔记详情
     * 
     * @param merchantId 商家ID
     * @param noteId     笔记ID
     * @return 笔记详情VO
     */
    NoteDetailVO getNoteDetail(Long merchantId, Long noteId);

    /**
     * 创建笔记
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param request    创建请求参数MAP
     * @return 新增笔记ID
     */
    Long createNote(Long merchantId, Long operatorId, Map<String, Object> request);

    /**
     * 更新笔记
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param noteId     笔记ID
     * @param request    更新请求参数MAP
     */
    void updateNote(Long merchantId, Long operatorId, Long noteId, Map<String, Object> request);

    /**
     * 发布笔记
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param noteId     笔记ID
     */
    void publishNote(Long merchantId, Long operatorId, Long noteId);

    /**
     * 下线笔记
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param noteId     笔记ID
     */
    void offlineNote(Long merchantId, Long operatorId, Long noteId);

    /**
     * 删除笔记
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param noteId     笔记ID
     */
    void deleteNote(Long merchantId, Long operatorId, Long noteId);

    /**
     * 获取笔记统计
     * 
     * @param merchantId 商家ID
     * @param noteId     笔记ID
     * @return 统计数据MAP
     */
    Map<String, Object> getNoteStats(Long merchantId, Long noteId);

    /**
     * 获取笔记评论列表
     * 
     * @param merchantId 商家ID
     * @param noteId     笔记ID
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 评论列表分页数据
     */
    PageResult<Map<String, Object>> getNoteComments(Long merchantId, Long noteId, Integer pageNum, Integer pageSize);

    /**
     * 创建笔记评论
     * 
     * @param merchantId 商家ID
     * @param operatorId 操作员ID
     * @param noteId     笔记ID
     * @param request    评论请求参数MAP
     * @return 新增评论ID
     */
    Long createNoteComment(Long merchantId, Long operatorId, Long noteId, Map<String, Object> request);
}
