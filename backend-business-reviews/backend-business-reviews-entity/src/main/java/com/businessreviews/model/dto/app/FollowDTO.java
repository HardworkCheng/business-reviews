package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 关注操作请求传输对象 (通用)
 * <p>
 * 用于关注各类主体，userId字段定义较为宽泛（String类型），建议优先使用具体的 FollowUserDTO
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class FollowDTO {

    @NotBlank(message = "用户ID不能为空")
    private String userId;
}
