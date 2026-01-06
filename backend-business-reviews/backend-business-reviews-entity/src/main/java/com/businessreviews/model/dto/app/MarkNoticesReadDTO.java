package com.businessreviews.model.dto.app;

import lombok.Data;

import java.util.List;

/**
 * 标记消息已读请求传输对象
 * <p>
 * 支持批量标记指定消息或全部标记已读
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class MarkNoticesReadDTO {

    /**
     * 通知ID列表，为空时表示全部已读
     */
    private List<Long> noticeIds;
}
