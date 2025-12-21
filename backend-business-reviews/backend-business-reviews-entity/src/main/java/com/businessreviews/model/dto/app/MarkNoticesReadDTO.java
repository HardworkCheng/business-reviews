package com.businessreviews.model.dto.app;

import lombok.Data;

import java.util.List;

/**
 * 用户端标记通知已读请求DTO
 */
@Data
public class MarkNoticesReadDTO {
    
    /**
     * 通知ID列表，为空时表示全部已读
     */
    private List<Long> noticeIds;
}
