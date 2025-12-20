package com.businessreviews.service;

import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.NoteItemVO;
import com.businessreviews.model.vo.NoteDetailVO;
import java.util.Map;

/**
 * 商家笔记服务接口
 */
public interface MerchantNoteService {
    
    /**
     * 获取笔记列表
     */
    PageResult<NoteItemVO> getNoteList(Long merchantId, Integer pageNum, Integer pageSize, 
            Integer status, Long shopId, String keyword);
    
    /**
     * 获取笔记详情
     */
    NoteDetailVO getNoteDetail(Long merchantId, Long noteId);
    
    /**
     * 创建笔记
     */
    Long createNote(Long merchantId, Long operatorId, Map<String, Object> request);
    
    /**
     * 更新笔记
     */
    void updateNote(Long merchantId, Long operatorId, Long noteId, Map<String, Object> request);
    
    /**
     * 发布笔记
     */
    void publishNote(Long merchantId, Long operatorId, Long noteId);
    
    /**
     * 下线笔记
     */
    void offlineNote(Long merchantId, Long operatorId, Long noteId);
    
    /**
     * 删除笔记
     */
    void deleteNote(Long merchantId, Long operatorId, Long noteId);
    
    /**
     * 获取笔记统计
     */
    Map<String, Object> getNoteStats(Long merchantId, Long noteId);

    /**
     * 获取笔记评论列表
     */
    PageResult<Map<String, Object>> getNoteComments(Long merchantId, Long noteId, Integer pageNum, Integer pageSize);

    /**
     * 商家发表评论
     */
    Long createNoteComment(Long merchantId, Long operatorId, Long noteId, Map<String, Object> request);
}
