package com.businessreviews.dto.mobile.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 移动端添加评论请求
 */
@Data
public class AddCommentRequest {
    
    @NotBlank(message = "笔记ID不能为空")
    private String noteId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    private String parentId;  // 父评论ID，回复时使用
}
