package com.businessreviews.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 标记通知已读请求
 */
@Data
public class MarkNoticesReadRequest {
    
    /**
     * 通知ID列表，为空时表示全部已读
     */
    private List<Long> noticeIds;
}
