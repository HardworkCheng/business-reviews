package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户端发表评论请求DTO
 */
@Data
public class PostCommentDTO {
    
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度应在1-500之间")
    private String content;
    
    /**
     * 父评论ID（回复评论时填写）
     */
    private Long parentId;
}
