package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分享笔记请求DTO
 */
@Data
public class ShareNoteDTO {
    
    /**
     * 笔记ID
     */
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;
    
    /**
     * 接收者用户ID列表
     */
    @NotEmpty(message = "接收者列表不能为空")
    private List<Long> userIds;
}
