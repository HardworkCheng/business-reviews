package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求
 */
@Data
public class PostCommentRequest {
    
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度应在1-500之间")
    private String content;
    
    /**
     * 父评论ID（回复评论时填写）
     */
    private Long parentId;
}
