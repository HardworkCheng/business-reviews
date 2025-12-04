package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCommentRequest {
    
    @NotBlank(message = "笔记ID不能为空")
    private String noteId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    private String parentId;  // 父评论ID，回复时使用
}
