package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户发布评论请求传输对象
 * <p>
 * 用于对笔记发表一级评论，或对已有评论进行回复（需指定parentId）。
 * </p>
 * 
 * @author businessreviews
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
