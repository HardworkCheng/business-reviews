package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发布评论请求传输对象
 * <p>
 * 用于用户在APP端发布对笔记的评论或对他人评论的回复
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class AddCommentDTO {

    @NotBlank(message = "笔记ID不能为空")
    private String noteId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private String parentId; // 父评论ID，回复时使用
}
