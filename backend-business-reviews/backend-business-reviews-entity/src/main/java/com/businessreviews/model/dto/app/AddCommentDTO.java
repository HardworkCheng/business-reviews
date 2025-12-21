package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户端添加评论请求DTO
 */
@Data
public class AddCommentDTO {
    
    @NotBlank(message = "笔记ID不能为空")
    private String noteId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    private String parentId;  // 父评论ID，回复时使用
}
