package com.businessreviews.service.merchant;

import com.businessreviews.common.PageResult;
import com.businessreviews.dto.response.NoteItemResponse;
import com.businessreviews.dto.response.NoteDetailResponse;
import java.util.Map;

/**
 * 商家笔记服务接口
 * 提供商家运营中心的笔记管理功能
 */
public interface MerchantNoteService {
    
    /**
     * 获取笔记列表
     */
    PageResult<NoteItemResponse> getNoteList(Long merchantId, Integer pageNum, Integer pageSize, 
            Integer status, Long shopId, String keyword);
    
    /**
     * 获取笔记详情
     */
    NoteDetailResponse getNoteDetail(Long merchantId, Long noteId);
    
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
}
